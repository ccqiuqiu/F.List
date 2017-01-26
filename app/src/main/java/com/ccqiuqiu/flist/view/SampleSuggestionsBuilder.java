package com.ccqiuqiu.flist.view;

import android.content.Context;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SampleSuggestionsBuilder implements SearchSuggestionsBuilder {
    private Context mContext;
    private List<SearchItem> mHistorySuggestions = new ArrayList<SearchItem>();

    public SampleSuggestionsBuilder(Context context) {
        this.mContext = context;
        createHistorys();
    }

    private void createHistorys() {
    }

    @Override
    public Collection<SearchItem> buildEmptySearchSuggestion(int maxCount) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        items.addAll(mHistorySuggestions);
        return items;
    }

    @Override
    public Collection<SearchItem> buildSearchSuggestion(int maxCount, String query) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        items.addAll(mHistorySuggestions);
        return items;
//        List<SearchItem> items = new ArrayList<SearchItem>();
//        if (query.startsWith("@")) {
//            SearchItem peopleSuggestion = new SearchItem(
//                    App.mContext.getString(R.string.search_account) + ": " + query.substring(1),
//                    query,
//                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
//            );
//            items.add(peopleSuggestion);
//        } else if (query.startsWith("#")) {
//            SearchItem toppicSuggestion = new SearchItem(
//                    App.mContext.getString(R.string.search_tag) + ": " + query.substring(1),
//                    query,
//                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
//            );
//            items.add(toppicSuggestion);
//        } else {
//            SearchItem peopleSuggestion = new SearchItem(
//                    App.mContext.getString(R.string.search_account) + ": " + query,
//                    "@" + query,
//                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
//            );
//            items.add(peopleSuggestion);
//            SearchItem toppicSuggestion = new SearchItem(
//                    App.mContext.getString(R.string.search_tag) + ": " + query,
//                    "#" + query,
//                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
//            );
//            items.add(toppicSuggestion);
//        }
//        for (SearchItem item : mHistorySuggestions) {
//            if (item.getValue().startsWith(query)) {
//                items.add(item);
//            }
//        }
//        return items;
    }
}
