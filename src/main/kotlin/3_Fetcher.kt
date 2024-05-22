//

interface MutableFetcher {

    fun getCompanyName(): String

    object Stub : MutableFetcher {

        override fun getCompanyName(): String = "Malt"
    }
}
