package org.example

data class SeqPresenter(
    val id: Int,
    var weight: Long,
    var mark: Marker = Marker.Free,
) {

    class SeqPresenterComparator {

        companion object : Comparator<SeqPresenter> {

            override fun compare(a: SeqPresenter, b: SeqPresenter): Int = -b.weight.compareTo(a.weight)
        }
    }
}
