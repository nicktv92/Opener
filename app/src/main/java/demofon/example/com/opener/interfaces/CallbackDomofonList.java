package demofon.example.com.opener.interfaces;

import java.util.List;

import demofon.example.com.opener.domofon.DomofonList;

public interface CallbackDomofonList {
    void onSuccess(List<DomofonList> domofonLists);

    void onRefresh();

    void onError(String error);
}
