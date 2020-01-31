package com.crabglory.microvideo.micro.selector.recycler;

interface AdapterCallback<Data> {
    void update(Data data, RecyclerAdapter.ViewHolder<Data> holder);
}
