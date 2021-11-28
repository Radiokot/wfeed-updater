package ua.com.radiokot.feed.updater.util

object ShittyPostChecker {
    private val SHITTY_TEXT_REGEX = ("( часы|подробности|розыгры|подпи(сан|шись|саться|сывайся)" +
            "|vk\\.cc/|сообществ|&#|интернет-магазин|инстаграм| трек|оффтоп|offtop|сервис|недорого" +
            "|shipping|sponsoring|теплофид|бесплатн|узнай|репост|creative cloud|ютубер|закажи" +
            "|iqoption|продолжение|рекоменд|10120976|блогер|нажми|предложение|инсту|конкурс" +
            "|скид(кой |ка )|загляни|ребят|читать|показать полностью|подолгу|club\\d+|follow me)").toRegex()

    fun isTextShitty(text: String) =
        SHITTY_TEXT_REGEX.containsMatchIn(text.toLowerCase())
}