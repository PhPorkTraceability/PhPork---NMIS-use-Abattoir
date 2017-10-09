package slaughter.phporktraceabilty.farmslaughter;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import helper.SQLiteHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public class AcceptedFragment extends Fragment {

    private static final String KEY_PIGID = "pig_id";
    private static final String KEY_LABEL = "label";
    private static final String KEY_BREED = "breed";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_WEEKF = "week_farrowed";
    private static final String KEY_FDATE = "farrowing_date";
    private static final String KEY_TAGRFID = "tag_rfid";
    private static SQLiteHandler db;
    private ListView lv;
    private ListAdapter adapter;

    public AcceptedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = SQLiteHandler.getInstance(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accepted, container, false);
        lv = (ListView) view.findViewById(R.id.pigsList);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> item = (HashMap<String, String>) lv.getItemAtPosition(i);
                HashMap<String, String> pigDetails = db.getPigDetails(item.get(KEY_LABEL));

                Dialog d = new Dialog(getActivity());
                d.setTitle("Pig Details");
                d.setContentView(R.layout.pig_details_layout);
                d.show();

                TextView pLabel = (TextView) d.findViewById(R.id.pigLabel);
                TextView pGender = (TextView) d.findViewById(R.id.pigGender);
                TextView pBreed = (TextView) d.findViewById(R.id.pigBreed);
                TextView pWeekF = (TextView) d.findViewById(R.id.pigWeekF);
                TextView pDateF = (TextView) d.findViewById(R.id.pigDateF);
                TextView pRFIDtag = (TextView) d.findViewById(R.id.pRFIDtag);

                pRFIDtag.setText(pigDetails.get(KEY_TAGRFID));
                pLabel.setText(pigDetails.get(KEY_LABEL));
                pGender.setText(pigDetails.get(KEY_GENDER));
                pBreed.setText(pigDetails.get(KEY_BREED));
                pWeekF.setText(pigDetails.get(KEY_WEEKF));
                pDateF.setText(pigDetails.get(KEY_FDATE));
            }
        });

        loadList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadList();

    }

    public void loadList() {
        ArrayList<HashMap<String, String>> pigs = db.getPigs("accepted");
        ArrayList<HashMap<String, String>> pigItems = new ArrayList<>();

        for (int i = 0; i < pigs.size(); i++) {
            HashMap<String, String> a = pigs.get(i);
            HashMap<String, String> b = new HashMap<>();
            b.put(KEY_LABEL, a.get(KEY_LABEL));
            b.put(KEY_BREED, a.get(KEY_BREED));
            b.put(KEY_GENDER, a.get(KEY_GENDER));
            pigItems.add(b);
        }

        adapter = new SimpleAdapter(
                getActivity(),
                pigItems,
                R.layout.list_item_layout,
                new String[]{KEY_LABEL, KEY_BREED, KEY_GENDER},
                new int[]{R.id.porkID, R.id.breed, R.id.gender}
        );

        lv.setAdapter(adapter);
    }
}
