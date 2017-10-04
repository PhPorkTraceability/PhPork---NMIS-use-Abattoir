package slaughter.phporktraceabilty.farmslaughter;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String KEY_PIGID = "pig_id";
    private static final String KEY_LABEL = "label";
    private static final String KEY_BREED = "breed";
    private static final String KEY_GENDER = "gender";
    List<Pig> pigList = null;
    private RecyclerView recycler;
    private MyAdapter adapter;
    private SQLiteHandler db;
    private SwipeRefreshLayout sr;
    private AlertDialog alertDialog;
    private CoordinatorLayout coordinatorLayout;
    private TextView textview;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = SQLiteHandler.getInstance(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
//        tTags = (TextView) view.findViewById(R.id.totalTags);
        textview = (TextView) view.findViewById(R.id.holdArea);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);
        recycler = (RecyclerView) view.findViewById(R.id.pigsList);
        sr = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String tag_string_req = "req_alldata";
                final String TAG = getActivity().getLocalClassName();

                // Request a string response from the provided URL.
                final JsonObjectRequest _request = new JsonObjectRequest(AppConfig.URL_GETALLDATA, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject resp) {

                                Log.d(TAG, "Getting tables : " + resp.toString());

                                try {
                                    //JSONObject resp = new JSONObject(response);
                                    boolean error = resp.getBoolean("error");

                                    // Check for error node in json
                                    if (!error) {
                                        getMovement(resp);
                                        loadList();
                                    } else {
                                        String errorMsg = resp.getString("error_msg");
                                        Log.e(TAG, "Error Response: " + errorMsg);
                                    }
                                } catch (JSONException e) {
                                    // JSON error
                                    try {
                                        e.printStackTrace();
                                        Log.e(TAG, "JSON Error: " + resp.toString(3));
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                            }

                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error: " + error.getMessage());
                        displayAlert("Connection failed.");
                    }
                });

                _request.setRetryPolicy(new DefaultRetryPolicy(
                        2000, //timeout in ms
                        0, // no of max retries
                        1)); // backoff multiplier

                // Adding request to request queue
                AppController.getInstance(getActivity())
                        .addToRequestQueue(_request, tag_string_req);

                sr.setRefreshing(false);

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadList();
    }

    public void loadList() {

        ArrayList<HashMap<String, String>> pigs = db.getPigsHome();
        pigList = new ArrayList<>();

        /*totalTags = pigs.size()<totalTags ? totalTags:pigs.size();

        tTags.setText(String.valueOf(totalTags));*/

        for (int i = 0; i < pigs.size(); i++) {
            HashMap<String, String> a = pigs.get(i);

            Pig item = new Pig(a.get(KEY_PIGID), a.get(KEY_LABEL),
                    a.get(KEY_BREED), a.get(KEY_GENDER), a.get("movement_id"));
            pigList.add(item);
        }

        adapter = new MyAdapter(getActivity(), pigList);
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(recycler);
        textview.setOnDragListener(new MyDragListener());
    }

    private ItemTouchHelper.Callback createHelperCallback() {
        return new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
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
                .make(coordinatorLayout, "Pig moved to " + status + " list.", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Pig is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                        undoUpdate(pID, movement_id, status);
                        loadList();
                    }
                });
        snackbar.show();
    }

    private void updateData(String pID, String movement_id, String status) {
        db.addSlaughterPigStat(status, pID);
        db.updateOnSwipe(movement_id, "old");
        db.updatePigStat(status, pID);
    }

    private void undoUpdate(String pID, String movement_id, String status) {
        db.removeSlaughterPigStat(status, pID);
        db.updateOnSwipe(movement_id, "new");
        db.updatePigStat("growing", pID);
    }

    public void getMovement(JSONObject resp) throws JSONException {
        final String KEY_DATE = "date_moved";
        final String KEY_TIME = "time_moved";
        final String KEY_SERVERDATE = "server_date";
        final String KEY_SERVERTIME = "server_time";
        final String KEY_PENID = "pen_id";
        JSONArray movement;

        movement = resp.getJSONArray("movement");

        for (int i = 0; i < movement.length(); i++) {
            JSONObject c = movement.getJSONObject(i);

            // Now store the user in SQLite
            String movement_id = c.getString("movement_id");
            String date_moved = c.getString(KEY_DATE);
            String time_moved = c.getString(KEY_TIME);
            String pen_id = c.getString(KEY_PENID);
            String server_date = c.getString(KEY_SERVERDATE);
            String server_time = c.getString(KEY_SERVERTIME);
            String pig_id = c.getString(KEY_PIGID);

            db.addMovement(movement_id, date_moved, time_moved, pen_id,
                    server_date, server_time, pig_id, "new");
            /*totalTags++;*/
        }

        if (movement.length() == 0) {
            displayAlert("No new data received.");
        }

    }

    public void displayAlert(String message) {

        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(message)
                .setCancelable(false)
                .setMessage("Try Again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            View view = (View) event.getLocalState();
            switch (action) {
                case DragEvent.ACTION_DRAG_ENDED:
                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DROP:
                    String pos = view.getTag().toString();
                    String pID = ((TextView) view.findViewById(R.id.pigID)).getText().toString();
                    Pig sel = pigList.get(Integer.parseInt(pos));
                    String movement_id = sel.movement_id;
                    pigList.remove(Integer.parseInt(pos));
                    adapter.notifyDataSetChanged();
                    showSnackbar("on hold", pID, movement_id);
                    view.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private final class MyLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    view);
            view.setVisibility(View.INVISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, view, 0);
            } else {
                view.startDrag(data, shadowBuilder, view, 0);
            }
            return false;
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private LayoutInflater inflater;
        private List<Pig> pigs;
        private Context context;
        private SQLiteHandler db;

        MyAdapter(Context context, List<Pig> pigs) {
            inflater = LayoutInflater.from(context);
            this.pigs = pigs;
            this.context = context;
            // setHasStableIds(true);
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

                itemView.setOnLongClickListener(new MyLongClickListener());
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        alertDialog = new AlertDialog.Builder(context)
//                                .setTitle("On Hold")
//                                .setCancelable(false)
//                                .setMessage("Add pig to On Hold List?")
//                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        String pos = itemView.getTag().toString();
//                                        Pig sel = pigs.get(Integer.parseInt(pos));
//                                        String movement_id = sel.movement_id;
//                                        String pID = pigId.getText().toString();
//                                        pigs.remove(Integer.parseInt(pos));
//                                        notifyDataSetChanged();
//                                        showSnackbar("on hold", pID, movement_id);
////                                        db.addSlaughterPigStat("on hold", pID);
////                                        db.updateOnSwipe(movement_id, "old");
////                                        db.updatePigStat("on hold", pID);
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.no, null)
//                                .show();
//                    }
//                });
            }
        }
    }
}

