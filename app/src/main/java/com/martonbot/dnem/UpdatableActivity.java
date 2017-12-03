package com.martonbot.dnem;

import android.app.Activity;
import android.widget.BaseAdapter;

public abstract class UpdatableActivity extends Activity {

    private BaseAdapter adapter;

    public void updateUI() {
        updateUiElements();
        adapter.notifyDataSetChanged();
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    protected abstract void refreshDataset();

    protected abstract void updateUiElements();

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
    }

    public void reloadData() {
        refreshDataset();
        updateUI();
    }
}
