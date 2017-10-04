package slaughter.phporktraceabilty.farmslaughter;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by marmagno on 5/22/2017.
 */

public class ReadyNowFragment extends Fragment {

    public ReadyNowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ready_now, container, false);
    }
}
