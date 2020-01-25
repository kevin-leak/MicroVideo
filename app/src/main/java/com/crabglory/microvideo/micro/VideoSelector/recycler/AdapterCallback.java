package com.crabglory.microvideo.micro.VideoSelector.recycler;

interface AdapterCallback<Data> {
    void update(Data data, RecyclerAdapter.ViewHolder<Data> holder);
}
