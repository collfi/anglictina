package cz.muni.fi.anglictina.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by collfi on 9. 2. 2016.
 */
public class Categories {
    public static final String[] categories = {
            "adj:all",
            "adj:pert",
            "adj:ppl",
            "adv:all",
            "adverbs",
            "animals",
            "body",
            "body_care",
            "buildings",
            "business_english_nouns",
            "business_english_verbs",
            "calendar",
            "cars",
            "city",
            "clothes",
            "colors",
            "compound_words",
            "computer",
            "eating_food_drinks",
            "family",
            "frequency",
            "gardening_and_plants",
            "geographic_features",
            "health",
            "holidays",
            "house",
            "imaginary_people_and_animals",
            "jobs",
            "law",
            "math",
            "money",
            "music",
            "nationalities",
            "noun:animal",
            "noun:artifact",
            "noun:body",
            "noun:cognition",
            "noun:communication",
            "noun:event",
            "noun:feeling",
            "noun:food",
            "noun:location",
            "noun:motive",
            "noun:object",
            "noun:person",
            "noun:phenomenon",
            "noun:plant",
            "noun:possession",
            "noun:process",
            "noun:quantity",
            "noun:relation",
            "noun:shape",
            "noun:state",
            "noun:substance",
            "noun:time",
            "noun:tops",
            "noun:attribute",
            "office",
            "other",
            "other:verb",
            "parts",
            "people",
            "prepositions",
            "pronunciation",
            "punctuation_marks",
            "reading",
            "science",
            "security",
            "school",
            "smoking",
            "sports",
            "tools",
            "transportation",
            "verb:body",
            "verb:cognition",
            "verb:communication",
            "verb:competition",
            "verb:consumption",
            "verb:creation",
            "verb:emotion",
            "verb:change",
            "verb:motion",
            "verb:perception",
            "verb:possession",
            "verb:social",
            "verb:stative",
            "verb:weather",
            "weather",
            "winter",
            "words_ending_in_ful",
            "words_ending_in_less",
    };

    public static final String[] categoriesForHuman = {
            "Podstatné jméno",
            "Přídavné jméno",
            "Sloveso",
            "Příslovce",
            "Předložky",
            "Auta",
            "Barvy",
            "Budovy",
            "Čas",
            "Části",
            "Čtení",
            "Domácnost",
            "Emoce",
            "Frekvence",
            "Hudba",
            "Imaginární postavy",
            "Interpunkční znaménka",
            "Jevy",
            "Jídlo & pití",
            "Kalendár",
            "Kancelář",
            "Komunikace",
            "Kouření",
            "Látky",
            "Lidé",
            "Lidské tělo",
            "Matematika",
            "Město",
            "Množství",
            "Národnosti",
            "Nástroje",
            "Obchod",
            "Objekty",
            "Oblečení",
            "Ostatní",
            "Péče o tělo",
            "Peníze",
            "Pocity",
            "Počasí",
            "Počítač",
            "Pohyb",
            "Poloha",
            "Poznání",
            "Procesy",
            "Přeprava",
            "Rodina",
            "Rostliny a zahrada",
            "Slova končící na -ful",
            "Slova končící na -less",
            "Sociální",
            "Soutěžení",
            "Sport",
            "Spotřeba",
            "Statické",
            "Stavy",
            "Svátky",
            "Škola",
            "Tvary",
            "Tvoření",
            "Události",
            "Věda",
            "Vlastnictví",
            "Vlastnosti",
            "Vnímaní",
            "Výslovnost",
            "Vztahy",
            "Zabezpečení",
            "Zákon",
            "Zaměstnání",
            "Zdraví",
            "Zeměpisné pojmy",
            "Zima",
            "Zložená slova",
            "Změna",
            "Zvířata"
    };

    public static final List<String> categoriesForHumanAscii = Arrays.asList(
            "Podstatne jmeno",
            "Pridavne jmeno",
            "Sloveso",
            "Prislovce",
            "Predlozky",
            "Auta",
            "Barvy",
            "Budovy",
            "Cas",
            "Casti",
            "Cteni",
            "Domacnost",
            "Emoce",
            "Frekvence",
            "Hudba",
            "Imaginarni postavy",
            "Interpunkcni znamenka",
            "Jevy",
            "Jidlo & piti",
            "Kalendar",
            "Kancelar",
            "Komunikace",
            "Koureni",
            "Latky",
            "Lide",
            "Lidske telo",
            "Matematika",
            "Mesto",
            "Mnozstvi",
            "Narodnosti",
            "Nastroje",
            "Obchod",
            "Objekty",
            "Obleceni",
            "Ostatni",
            "Pece o telo",
            "Penize",
            "Pocity",
            "Pocasi",
            "Pocitac",
            "Pohyb",
            "Poloha",
            "Poznani",
            "Procesy",
            "Preprava",
            "Rodina",
            "Rostliny a zahrada",
            "Slova koncici na -ful",
            "Slova koncici na -less",
            "Socialni",
            "Soutezeni",
            "Sport",
            "Spotreba",
            "Staticke",
            "Stavy",
            "Svatky",
            "Skola",
            "Tvary",
            "Tvoreni",
            "Udalosti",
            "Veda",
            "Vlastnictvi",
            "Vlastnosti",
            "Vnimani",
            "Vyslovnost",
            "Vztahy",
            "Zabezpeceni",
            "Zakon",
            "Zamestnani",
            "Zdravi",
            "Zemepisne pojmy",
            "Zima",
            "Zlozena slova",
            "Zmena",
            "Zvirata"
    );

//    public static String toHuman(String c) {
//        for (int i = 0; i < categories.length; i++) {
//            if (categories[i].equals(c)) {
//                return categoriesForHuman[i];
//            }
//        }
//        return c;
//    }
//
//    public static String fromHuman(String c) {
//        for (int i = 0; i < categoriesForHuman.length; i++) {
//            if (categoriesForHuman[i].equals(c)) {
//                return categories[i];
//            }
//        }
//        return c;
//    }
}
