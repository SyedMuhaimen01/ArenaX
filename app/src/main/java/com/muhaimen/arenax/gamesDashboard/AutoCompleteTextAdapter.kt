import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class GameAutoCompleteAdapter(
    context: Context,
    private val gamesList: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private var filteredGamesList: List<String> = gamesList

    override fun getCount(): Int {
        return filteredGamesList.size
    }

    override fun getItem(position: Int): String? {
        return filteredGamesList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {
                    // Filter logic to find games that match the user's input
                    val filteredList = gamesList.filter {
                        it.contains(constraint.toString(), ignoreCase = true)
                    }

                    // Update filter results with the filtered list
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                // Update the adapter's data with the filtered results
                filteredGamesList = if (results != null && results.count > 0) {
                    results.values as List<String>
                } else {
                    emptyList()
                }
                notifyDataSetChanged()  // Notify the adapter to update the suggestions
            }
        }
    }
}
