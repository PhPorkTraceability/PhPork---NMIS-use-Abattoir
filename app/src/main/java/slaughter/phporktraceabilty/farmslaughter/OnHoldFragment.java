package slaughter.phporktraceabilty.farmslaughter;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helper.SQLiteHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class OnHoldFragment extends Fragment {

    private static final String KEY_PIGID = "pig_id";
    private static final String KEY_LABEL = "label";
    private static final String KEY_BREED = "breed";
    private static final String KEY_GENDER = "gender";
    private static SQLiteHandler db;
    private RecyclerView recycler;
    private MyAdapter adapter;
    private List<Pig> pigList;
    private View v;

    public OnHoldFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        db = SQLiteHandler.getInstance(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_hold, container, false);
        recycler = (RecyclerView) view.findViewById(R.id.pigsList);

        loadList();
        v = view;
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        loadList();
    }


    public void loadList() {
        ArrayList<HashMap<String, String>> pigs = db.getPigs("on hold");
        pigList = new ArrayList<>();
        for (int i = 0; i < pigs.size(); i++) {
            HashMap<String, String> a = pigs.get(i);
            Pig item = new Pig(a.get(KEY_PIGID), a.get(KEY_LABEL), a.get(KEY_BREED), a.get(KEY_GENDER), "0");
            pigList.add(item);
        }

        adapter = new MyAdapter(getActivity(), pigList);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(recycler);
    }


    private ItemTouchHelper.Callback createHelperCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String pID = ((TextView) viewHolder.itemView.findViewById(R.id.pigID)).getText().toString();
                String pos = viewHolder.itemView.getTag().toString();
                Pig sel = pigList.get(Integer.parseInt(pos));
                String movement_id = sel.movement_id;

                pigList.remove(Integer.parseInt(pos));
                adapter.notifyDataSetChanged();

                if (direction == ItemTouchHelper.RIGHT) {
                    showSnackbar("accepted", pID, movement_id);
                } else {
                    showSnackbar("rejected", pID, movement_id);
                }
            }
        };
    }

    public void showSnackbar(final String status, final String pID, final String movement_id) {
        updateData(pID, movement_id, status);

        Snackbar snackbar = Snackbar
                .make(v, "Pig moved to " + status + " list.", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar snackbar1 = Snackbar.make(v, "Pig is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        undoUpdate(pID, movement_id);
                        loadList();
                    }
                });
        snackbar.show();
    }

    private void updateData(String pID, String movement_id, String status) {
        db.addSlaughterPigStat(status, pID);
        db.updateOnSwipe(movement_id, "old", pID);
        db.updatePigStat(status, pID);
    }

    private void undoUpdate(String pID, String movement_id) {
        db.updateOnSwipe(movement_id, "old", pID);
        db.updatePigStat("on hold", pID);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private LayoutInflater inflater;
        private List<Pig> pigs;
        private Context context;
        private SQLiteHandler db;
        private AlertDialog alertDialog;

        MyAdapter(Context context, List<Pig> pigs) {
            inflater = LayoutInflater.from(context);
            this.pigs = pigs;
            this.context = context;
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.list_item_layout, parent, false);

            return new MyAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            Pig item = pigs.get(position);
            holder.porkId.setText(item.getLabel());
            holder.breed.setText(item.getBreed());
            holder.gender.setText(item.getGender());
            holder.pigId.setText(item.getPorkId());
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return pigs.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView porkId;
            TextView pigId;
            TextView breed;
            TextView gender;
            RelativeLayout rl;

            MyViewHolder(final View itemView) {
                super(itemView);

                db = SQLiteHandler.getInstance(context);
                porkId = (TextView) itemView.findViewById(R.id.porkID);
                pigId = (TextView) itemView.findViewById(R.id.pigID);
                breed = (TextView) itemView.findViewById(R.id.breed);
                gender = (TextView) itemView.findViewById(R.id.gender);
                rl = (RelativeLayout) itemView.findViewById(R.id.listLayout);

            }
        }
    }

}
