package com.example.domain

import java.text.Normalizer
import java.util.Locale

object ExpenseCategorizer {

    private val supermarketKeywords = listOf(
        "supermercado", "mercado", "hipermercado", "carrefour", "pão de açúcar", "pao de acucar",
        "assai", "atacadão", "atacadao", "extra", "hortifruti", "sacolão", "sacolao", "feira",
        "açougue", "acougue", "mercearia", "leite", "arroz", "feijão", "feijao", "fruta",
        "verdura", "legumes", "padaria", "compras do mês", "compras mes", "dia",
        "sonda", "mambo", "zaffari", "savegnago", "atacado", "dispensa", "limpeza"
    )

    private val transportKeywords = listOf(
        "transporte", "uber", "99", "99app", "99pop", "táxi", "taxi", "gasolina", "combustível",
        "combustivel", "álcool", "etanol", "diesel", "posto", "ipiranga", "shell", "br",
        "petrobras", "metrô", "metro", "ônibus", "onibus", "passagem", "estacionamento",
        "pedágio", "pedagio", "bilhete único", "bilhete unico", "recarga bilhete", "valet",
        "troca de óleo", "oleo", "pneu", "oficina", "mecanico", "ipva", "sem parar", "conectcar"
    )

    private val healthKeywords = listOf(
        "saúde", "saude", "farmácia", "farmacia", "drogaria", "droga raia", "drogasil",
        "pague menos", "remédio", "remedio", "medicamento", "médico", "medico",
        "doutor", "consulta", "exame", "laboratório", "dentista", "plano de saúde", "unimed",
        "sulamerica", "bradesco saude", "psicólogo", "psicologo", "terapia", "academia",
        "smart fit", "bluefit", "suplemento", "ótica", "otica", "oftalmo", "hospital", "pronto socorro"
    )

    private val foodKeywords = listOf(
        "almoço", "almoco", "jantar", "janta", "comida", "refeição", "refeicao", "restaurante",
        "ifood", "rappi", "lanche", "mcdonald", "mc donalds", "burger king", "bk", "hamburguer",
        "hamburgueria", "pizza", "pizzaria", "café", "cafe", "cafeteria", "salgado",
        "sobremesa", "sorvete", "açaí", "acai", "marmita", "pastel", "churrascaria", "sushi",
        "japonês", "japones", "quilo", "self service", "subway", "starbucks", "doceria"
    )

    private val housingKeywords = listOf(
        "moradia", "aluguel", "condomínio", "condominio", "luz", "energia", "enel", "cpfl",
        "light", "cemig", "copel", "água", "agua", "sabesp", "copasa", "sanepar", "gás", "gas",
        "ultragaz", "liquigas", "internet", "fibra", "claro", "vivo", "tim", "iptu", "faxina",
        "diarista", "manutenção", "reforma", "material de construção", "leroy", "tok&stok"
    )

    private val leisureKeywords = listOf(
        "lazer", "cinema", "cinemark", "ingresso", "show", "teatro", "bar", "boteco",
        "cerveja", "chopp", "chope", "balada", "festa", "netflix", "spotify", "steam",
        "playstation", "psn", "xbox", "game", "jogo", "passeio", "viagem", "hotel", "pousada",
        "praia", "parque", "streaming", "disney", "hbo", "prime video", "clube", "evento", "churrasco"
    )

    fun categorize(text: String): ExpenseCategory {
        val normalized = normalize(text)
        val tokens = normalized.split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }

        fun matches(keywords: List<String>): Boolean {
            return keywords.any { keyword ->
                val normKw = normalize(keyword)
                if (normKw.contains(" ")) {
                    normalized.contains(normKw)
                } else if (normKw.length <= 4) {
                    tokens.contains(normKw)
                } else {
                    tokens.any { it.contains(normKw) } || normalized.contains(normKw)
                }
            }
        }

        // Check categories in priority order
        if (matches(supermarketKeywords)) return ExpenseCategory.SUPERMERCADO
        if (matches(transportKeywords)) return ExpenseCategory.TRANSPORTE
        if (matches(healthKeywords)) return ExpenseCategory.SAUDE
        if (matches(foodKeywords)) return ExpenseCategory.ALIMENTACAO
        if (matches(housingKeywords)) return ExpenseCategory.MORADIA
        if (matches(leisureKeywords)) return ExpenseCategory.LAZER

        return ExpenseCategory.OUTROS
    }

    private fun normalize(str: String): String {
        val decomposed = Normalizer.normalize(str.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        return decomposed.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").trim()
    }
}
