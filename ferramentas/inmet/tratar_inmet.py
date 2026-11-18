#!/usr/bin/env python3
"""
Trata arquivos horarios do INMET (CSV ou XLSX) e gera dados enxutos para o SMField.

Uso (o jeito mais simples: aponte para a PASTA com os ZIPs baixados do INMET):
    python tratar_inmet.py PASTA --estacao A502
    python tratar_inmet.py 2023.zip 2024.zip 2025.zip --estacao A502
    python tratar_inmet.py arquivo1.csv arquivo2.csv
Aceita pasta, .zip (le os CSV de dentro sem extrair), .csv, .xlsx e curingas.
Com ZIP anual use --estacao, senao ele le as centenas de estacoes do ZIP.

Varios anos da MESMA estacao devem ser passados juntos:
o script calcula a media dos anos para cada mes (clima tipico).

Saidas (na pasta --saida, padrao "saida_inmet"):
  estacoes.csv          catalogo: codigo, nome, UF, latitude, longitude, altitude, cobertura, meses validos
  diario_consolidado.csv  um dia por linha, todas as estacoes (temp_media, temp_min, umidade, chuva)
  mensal.csv            resumo mensal por estacao e ano
  regioes.json          formato do DadosLoader (nome, uf, 12 meses). So estacoes COMPLETAS.
  relatorio.txt         o que foi aproveitado e o que foi descartado, por mes
"""
import argparse
import glob
import io
import json
import sys
import zipfile
from pathlib import Path

EXTENSOES = (".csv", ".xlsx", ".xlsm", ".zip")

import pandas as pd

LIMITE_GEADA_C = 3.0       # minima diaria do ar <= 3 C conta como dia de risco de geada (ESTIMATIVA)
MIN_HORAS_DIA = 20         # dia so vale se tiver >= 20 das 24 horas
MIN_DIAS_MES = 0.80        # mes so vale se >= 80% dos dias forem validos
FUSO_LOCAL_H = -3          # os dados vem em UTC; Brasilia = UTC-3


def achar(colunas, *trechos):
    """Encontra a coluna cujo nome contem todos os trechos (sem depender de acento)."""
    for c in colunas:
        n = str(c).upper()
        if all(t in n for t in trechos):
            return c
    raise KeyError(f"Coluna nao encontrada: {trechos}")


def numero(serie):
    """Texto com virgula decimal -> numero. Vazio e -9999 viram NaN (sem dado, NAO zero)."""
    s = pd.to_numeric(serie.astype(str).str.strip().str.replace(",", ".", regex=False), errors="coerce")
    return s.mask(s == -9999)


def ler(fonte, nome: str):
    """Le um arquivo do INMET (caminho ou bytes). Devolve (metadados, DataFrame horario enxuto)."""
    if nome.lower().endswith((".xlsx", ".xlsm")):
        bruto = pd.read_excel(fonte, header=None, dtype=str).fillna("")
        # a planilha convertida traz uma linha 'Column1..' antes dos metadados
        ini = next(i for i, v in enumerate(bruto[0]) if str(v).startswith("REGIAO"))
        linhas = bruto.iloc[ini:].reset_index(drop=True)
    else:
        # names=range(25) forca largura fixa: as 8 linhas de metadados tem so 2 campos
        # e, sem isso, o pandas descartaria as linhas de dados.
        bruto = pd.read_csv(fonte, sep=";", header=None, names=range(25), dtype=str,
                            encoding="latin-1", skip_blank_lines=False).fillna("")
        linhas = bruto
    meta = {str(linhas.iloc[i, 0]).strip(": ").upper(): str(linhas.iloc[i, 1]).strip() for i in range(8)}
    cab = linhas.iloc[8].tolist()
    d = linhas.iloc[9:].copy()
    d.columns = cab

    c_data = achar(cab, "DATA")
    c_hora = achar(cab, "HORA")
    c_chuva = achar(cab, "PRECIPITA")
    c_temp = achar(cab, "TEMPERATURA DO AR")
    c_tmin = achar(cab, "TEMPERATURA M", "NIMA")
    c_ur = achar(cab, "UMIDADE RELATIVA DO AR")

    datas = d[c_data].astype(str).str.strip().str.replace("-", "/", regex=False)
    horas = d[c_hora].astype(str).str.extract(r"(\d{2})")[0].str.zfill(2)
    horas = horas.fillna("00")
    utc = pd.to_datetime(datas + " " + horas, format="mixed", errors="coerce")
    utc = utc.where(utc.notna(), pd.to_datetime(datas + " " + horas, format="%d/%m/%Y %H", errors="coerce"))

    out = pd.DataFrame({
        "local": utc + pd.Timedelta(hours=FUSO_LOCAL_H),
        "chuva": numero(d[c_chuva]),
        "temp": numero(d[c_temp]),
        "tmin": numero(d[c_tmin]),
        "ur": numero(d[c_ur]),
    }).dropna(subset=["local"])
    return meta, out


def diario(h: pd.DataFrame) -> pd.DataFrame:
    h = h.copy()
    h["dia"] = h["local"].dt.normalize()
    g = h.groupby("dia")
    dia = pd.DataFrame({
        "horas_temp": g["temp"].count(),
        "horas_chuva": g["chuva"].count(),
        "temp_media": g["temp"].mean(),
        "temp_min": g["tmin"].min().combine_first(g["temp"].min()),
        "umidade_ar_media": g["ur"].mean(),
        "chuva_mm": g["chuva"].sum(min_count=1),
    })
    # dia incompleto -> NaN (nunca zero)
    dia.loc[dia.horas_temp < MIN_HORAS_DIA, ["temp_media", "temp_min", "umidade_ar_media"]] = float("nan")
    dia.loc[dia.horas_chuva < MIN_HORAS_DIA, "chuva_mm"] = float("nan")
    return dia


def mensal(dia: pd.DataFrame) -> pd.DataFrame:
    """Um registro por (ano, mes) com temp_media, chuva_mm e prob_geada; mes fraco vira NaN."""
    d = dia.copy()
    d["ano"], d["mes"] = d.index.year, d.index.month
    linhas = []
    for (ano, mes), g in d.groupby(["ano", "mes"]):
        dias_no_mes = pd.Period(f"{ano}-{mes:02d}").days_in_month
        ok_t = g["temp_media"].notna().sum()
        ok_c = g["chuva_mm"].notna().sum()
        ok_m = g["temp_min"].notna().sum()
        linhas.append({
            "ano": ano, "mes": mes,
            "temp_media": g["temp_media"].mean() if ok_t >= MIN_DIAS_MES * dias_no_mes else float("nan"),
            # chuva: soma dos dias validos, ajustada para o mes inteiro
            "chuva_mm": g["chuva_mm"].sum() / ok_c * dias_no_mes if ok_c >= MIN_DIAS_MES * dias_no_mes else float("nan"),
            "prob_geada": (g["temp_min"] <= LIMITE_GEADA_C).sum() / ok_m if ok_m >= MIN_DIAS_MES * dias_no_mes else float("nan"),
            "cobertura_dias": int(min(ok_t, ok_c)),
            "dias_no_mes": dias_no_mes,
        })
    return pd.DataFrame(linhas)


def coletar(entradas, estacoes):
    """Expande pastas, curingas e ZIPs em uma lista de (nome, fonte).
    `estacoes` (ex.: {"A502"}) filtra pelo codigo no nome do arquivo; vazio = todas."""
    itens = []
    for e in entradas:
        p = Path(e)
        if p.is_dir():
            candidatos = sorted(x for x in p.rglob("*") if x.suffix.lower() in EXTENSOES)
        elif any(ch in e for ch in "*?["):
            candidatos = sorted(Path(x) for x in glob.glob(e, recursive=True))
        elif p.is_file():
            candidatos = [p]
        else:
            sys.exit(f"ERRO: nao encontrei '{e}'. Confira o caminho (use o nome REAL do arquivo, "
                     f"ou apenas a pasta onde estao os arquivos/ZIPs).")
        for c in candidatos:
            if c.suffix.lower() == ".zip":
                with zipfile.ZipFile(c) as z:
                    for nome in sorted(z.namelist()):
                        if nome.lower().endswith(".csv") and _passa(nome, estacoes):
                            itens.append((f"{c.name}:{nome}", io.BytesIO(z.read(nome)), nome))
            elif c.suffix.lower() in EXTENSOES and _passa(c.name, estacoes):
                itens.append((str(c), c, c.name))
    return itens


def _passa(nome, estacoes):
    return not estacoes or any(f"_{s}_" in nome.upper() for s in estacoes)


def numero_meta(txt):
    try:
        return float(str(txt).replace(",", "."))
    except ValueError:
        return float("nan")


def juntar_dias(lista):
    """Junta os dias de varios arquivos da mesma estacao; se um dia aparece em dois, fica o mais completo."""
    d = pd.concat(lista)
    d = d.sort_values("horas_temp", ascending=False)
    d = d[~d.index.duplicated(keep="first")]
    return d.sort_index()


def main():
    ap = argparse.ArgumentParser(description="Trata dados horarios do INMET para o SMField.")
    ap.add_argument("entradas", nargs="+", help="pasta(s), ZIP(s), arquivo(s) .csv/.xlsx ou curingas")
    ap.add_argument("--estacao", action="append", default=[],
                    help="codigo da estacao (ex.: A502); pode repetir. Sem isso, le TODAS as estacoes.")
    ap.add_argument("--saida", default="saida_inmet")
    a = ap.parse_args()
    saida = Path(a.saida)
    saida.mkdir(parents=True, exist_ok=True)

    itens = coletar(a.entradas, {s.upper() for s in a.estacao})
    if not itens:
        sys.exit("ERRO: nenhum arquivo .csv/.xlsx/.zip encontrado (ou nenhum bate com --estacao).")
    total = len(itens)
    print(f"{total} arquivo(s) para ler. Pode levar alguns minutos...")

    por_estacao, erros = {}, []
    for i, (rotulo, fonte, nome) in enumerate(itens, 1):
        try:
            meta, h = ler(fonte, nome)
            dia = diario(h)          # resume em dias ja aqui: nao guarda as 7 mil linhas horarias
        except Exception as ex:      # um arquivo ruim nao derruba o resto
            erros.append(f"{rotulo}: {type(ex).__name__}: {ex}")
            continue
        cod = meta.get("CODIGO (WMO)", "SEM_CODIGO")
        e = por_estacao.setdefault(cod, {"meta": meta, "dias": []})
        e["dias"].append(dia)
        if i % 25 == 0 or i == total:
            print(f"  {i}/{total} arquivos lidos")

    catalogo, diarios, mensais, regioes, rel = [], [], [], [], []
    nomes_vistos = {}
    for cod, e in sorted(por_estacao.items()):
        meta = e["meta"]
        dia = juntar_dias(e["dias"])
        m = mensal(dia)
        nome = meta.get("ESTACAO", cod).title()
        uf = meta.get("UF", "")

        validos_dia = dia[dia.temp_media.notna() | dia.chuva_mm.notna()]
        d_out = validos_dia.drop(columns=["horas_temp", "horas_chuva"]).round(2)
        d_out.insert(0, "estacao", cod)
        diarios.append(d_out.reset_index(names="data"))
        m_out = m.copy()
        m_out.insert(0, "estacao", cod)
        mensais.append(m_out.round(2))

        meses_ok = sorted(m[m.temp_media.notna() & m.chuva_mm.notna()].mes.unique().tolist())
        esperado = (dia.index.max() - dia.index.min()).days + 1 if len(dia) else 0
        catalogo.append({
            "estacao": cod, "nome": nome, "uf": uf, "regiao": meta.get("REGIAO", ""),
            "latitude": numero_meta(meta.get("LATITUDE")), "longitude": numero_meta(meta.get("LONGITUDE")),
            "altitude_m": numero_meta(meta.get("ALTITUDE")),
            "dias_com_dado": int(len(validos_dia)), "dias_no_periodo": int(esperado),
            "cobertura_pct": round(100 * len(validos_dia) / esperado, 1) if esperado else 0,
            "meses_validos": ",".join(map(str, meses_ok)),
        })

        tipico = []
        for mes in range(1, 13):
            v = m[(m.mes == mes) & m.temp_media.notna() & m.chuva_mm.notna()]
            tipico.append(None if v.empty else {
                "temp_media": round(float(v.temp_media.mean()), 1),
                "chuva_mm": round(float(v.chuva_mm.mean())),
                "prob_geada": round(float(v.prob_geada.mean()), 2)})
        completa = all(t is not None for t in tipico)
        if completa:
            if nome in nomes_vistos:
                rel.append(f"AVISO: nome '{nome}' repetido ({nomes_vistos[nome]} e {cod}); o recomendador busca por nome.")
            nomes_vistos[nome] = cod
            regioes.append({"nome": nome, "uf": uf, "meses": tipico})
        rel.append(f"{cod} {nome}/{uf}: meses validos [{','.join(map(str, meses_ok)) or 'nenhum'}] "
                   f"cobertura {catalogo[-1]['cobertura_pct']}% -> "
                   f"{'ENTROU no regioes.json' if completa else 'fora do regioes.json (faltam meses)'}")

    pd.DataFrame(catalogo).to_csv(saida / "estacoes.csv", index=False, encoding="utf-8-sig")
    pd.concat(diarios).to_csv(saida / "diario_consolidado.csv", index=False, encoding="utf-8-sig")
    pd.concat(mensais).to_csv(saida / "mensal.csv", index=False, encoding="utf-8-sig")
    (saida / "regioes.json").write_text(json.dumps(regioes, ensure_ascii=False, indent=1), encoding="utf-8")
    corpo = rel + (["", "ARQUIVOS COM ERRO:"] + erros if erros else [])
    (saida / "relatorio.txt").write_text("\n".join(corpo) + "\n", encoding="utf-8")

    print(f"\nEstacoes processadas: {len(catalogo)} | no regioes.json: {len(regioes)} | arquivos com erro: {len(erros)}")
    for x in erros[:5]:
        print("  erro:", x)
    print(f"Arquivos gerados em: {saida.resolve()}")


if __name__ == "__main__":
    sys.exit(main())
