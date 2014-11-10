package com.octgn.library.unnamed;

import android.content.Context;
import android.util.Log;

import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.PkRSS;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class NugetFeedReader implements IFeedReader {
    final String[] mFeeds;
    final Context mContext;

    public NugetFeedReader(Context context, String... feeds)
    {
        mFeeds = feeds;
        mContext = context;
    }

    @Override
    public Iterator<IFeedGame> getGames() {
        return new FeedIterator(mFeeds);
    }

    class FeedIterator implements Iterator<IFeedGame> {
        final String[] mFeeds;
        int mCurrentFeed;

        public FeedIterator(String[] feeds)
        {
            mFeeds = feeds;
            mCurrentFeed = 0;
        }

        @Override
        public boolean hasNext() {
            try {
                List<Article> list = PkRSS.with(mContext).load(mFeeds[0]).get();
                Log.d("",list.get(0).toString());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public IFeedGame next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }
}

