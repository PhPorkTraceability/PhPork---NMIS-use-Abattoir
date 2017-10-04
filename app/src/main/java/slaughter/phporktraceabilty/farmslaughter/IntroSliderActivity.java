package slaughter.phporktraceabilty.farmslaughter;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;

import helper.IntroSliderSession;
import listeners.OnSwipeTouchListener;

/**
 * Created by marmagno on 5/22/2017.
 */

public class IntroSliderActivity extends AppCompatActivity {
    private final Fragment[] f;
    IntroSliderSession introLogin;
    private int mIndex;
    private Button next_btn;
    private Button prev_btn;

    public IntroSliderActivity() {
        super();
        Fragment[] tempF = {
                new WelcomeFragment(),
                new IntroductionFragment(),
                new HowToUseFragment(),
                new HowToUseFragment2(),
                new HowToUseFragment3(),
                new HowToUseFragment4()
        };

        this.mIndex = 0;
        this.f = tempF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        introLogin = new IntroSliderSession(getApplicationContext());
        introLogin.setLogin();

        final int containerID = R.id.container;

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final RelativeLayout container = (RelativeLayout) findViewById(containerID);

        next_btn = (Button) findViewById(R.id.next_btn);
        prev_btn = (Button) findViewById(R.id.prev_btn);
        animateButton();

        ft.replace(containerID, f[mIndex]);
        ft.commit();

        if (next_btn != null) {
            next_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextFragment(containerID);
                }
            });
        } else {
            Log.e(IntroSliderActivity.class.toString(), "next_btn is null.");
        }

        if (prev_btn != null) {
            prev_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevFragment(containerID);
                }
            });
        } else {
            Log.e(IntroSliderActivity.class.toString(), "prev_btn is null.");
        }

        if (container != null) {
            container.setOnTouchListener(new OnSwipeTouchListener(IntroSliderActivity.this) {
                @Override
                public void onSwipeRight() {
                    prevFragment(containerID);
                }

                @Override
                public void onSwipeLeft() {
                    nextFragment(containerID);
                }
            });
        }
    }

    private void nextFragment(int containerID) {
        if (mIndex < f.length - 1) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            ft.replace(containerID, f[++mIndex]);
            animateButton();
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Intent i = new Intent(IntroSliderActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void prevFragment(int containerID) {
        if (mIndex > 0) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
            ft.replace(containerID, f[--mIndex]);
            animateButton();
            ft.commit();
        }
    }

    private void animateButton() {
        next_btn.setText((mIndex >= f.length - 1) ? "Go!" : "Next");
        if (prev_btn.getVisibility() == View.VISIBLE && mIndex <= 0) {
            fadeButton(true);
        } else if (prev_btn.getVisibility() == View.INVISIBLE && mIndex > 0) {
            fadeButton(false);
        }
    }

    //code from http://stackoverflow.com/questions/20782260/
    private void fadeButton(final boolean isFadeOut) {
        Animation fade;
        if (isFadeOut) {
            fade = new AlphaAnimation(1, 0);
        } else {
            fade = new AlphaAnimation(0, 1);
        }

        fade.setInterpolator(new AccelerateInterpolator());
        fade.setDuration(500);

        fade.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (isFadeOut) {
                    prev_btn.setVisibility(View.INVISIBLE);
                } else {
                    prev_btn.setVisibility(View.VISIBLE);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        prev_btn.startAnimation(fade);
    }
}
