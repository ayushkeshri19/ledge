package com.ayush.sms.data.local.classifier

import com.ayush.sms.domain.classifier.ClassifierRule
import com.ayush.sms.domain.classifier.MatchType
import com.ayush.sms.domain.classifier.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BakedInClassifierRules @Inject constructor() {

    val rules: List<ClassifierRule> = buildList {
        food("swiggy", "zomato", "starbucks", "dominos")
        shopping("amazon", "amzn", "flipkart", "myntra", "blinkit", "zepto", "dmart")
        transport("uber", "ola cabs", "rapido", "irctc", "indianoil", "hpcl", "bpcl", "fastag")
        entertainment("netflix", "spotify", "prime video", "hotstar", "bookmyshow")
        health("apollo", "medplus", "pharmeasy", "1mg", "cult.fit")
        bills("jio", "airtel", "vodafone", "bsnl", "recharge", "tata power", "tata sky")
        billsAggregator("dreamplug", "cred", "billdesk")
        housing("nobroker")
        subscriptions("github", "microsoft 365", "google one")
        investments("zerodha", "groww")
        education("byjus", "unacademy", "coursera")
        insurance("lic ", "hdfc life", "acko")
    }

    private fun MutableList<ClassifierRule>.food(vararg k: String) = add(k, "FOOD", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.shopping(vararg k: String) = add(k, "SHOPPING", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.transport(vararg k: String) = add(k, "TRANSPORT", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.entertainment(vararg k: String) =
        add(k, "ENTERTAINMENT", Source.CURATED, 10)

    private fun MutableList<ClassifierRule>.health(vararg k: String) = add(k, "HEALTH", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.bills(vararg k: String) = add(k, "BILLS", Source.CURATED, 8)
    private fun MutableList<ClassifierRule>.billsAggregator(vararg k: String) =
        add(k, "BILLS", Source.AGGREGATOR_DEFAULT, 5)

    private fun MutableList<ClassifierRule>.housing(vararg k: String) = add(k, "HOUSING", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.subscriptions(vararg k: String) =
        add(k, "SUBSCRIPTIONS", Source.CURATED, 10)

    private fun MutableList<ClassifierRule>.investments(vararg k: String) = add(k, "INVESTMENTS", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.education(vararg k: String) = add(k, "EDUCATION", Source.CURATED, 10)
    private fun MutableList<ClassifierRule>.insurance(vararg k: String) = add(k, "INSURANCE", Source.CURATED, 10)

    private fun MutableList<ClassifierRule>.add(
        keywords: Array<out String>,
        slug: String,
        source: Source,
        priority: Int
    ) {
        keywords.forEach { keyword ->
            add(ClassifierRule(keyword, slug, MatchType.SUBSTRING, priority, source))
        }
    }
}