package me.gurpreetsk.emicalulator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gurpreetsk.emicalulator.R;
import me.gurpreetsk.emicalulator.model.Emi;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edittext_principal_amount)
    MaterialEditText edittextPrincipal;
    @BindView(R.id.edittext_duration)
    MaterialEditText edittextDuration;
    @BindView(R.id.button_calculate_emi)
    Button buttonCalculateEmi;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        buttonCalculateEmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EMI = [P x R x (1+R)^N]/[(1+R)^N-1],
                // where P stands for the loan amount or principal,
                // R is the interest rate per month [if the interest rate per annum is 11%, then the rate of interest will be 11/(12 x 100)],
                // and N is the number of monthly instalments.

                final long principal = Long.parseLong(edittextPrincipal.getText().toString().trim());
                final double rate = 0.03;
                final long duration = Long.parseLong(edittextDuration.getText().toString().trim());
                double EMI = principal * rate * (Math.pow(1 + rate, duration) / (Math.pow(1 + rate, duration) - 1));

                Log.i(TAG, "principal: " + principal);
                Log.i(TAG, "duration: " + duration);
                Log.i(TAG, "calculateEmi: " + String.format("%.2f", EMI));

                String details = "Your monthly EMI is " + String.format("%.2f", EMI) + " for " + duration + " months.\n"
                        + "A total amount of " + String.format("%.2f", EMI * duration) + " is to be paid by you.";

                new MaterialDialog.Builder(MainActivity.this)
                        .title("Your details")
                        .content(details)
                        .positiveText(R.string.show_full_report)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //TODO set content provider and db
                                Intent intent = new Intent(MainActivity.this, EmiListActivity.class);
                                ArrayList<Emi> emis = calcAllEmis(principal, rate, duration);
                                if (emis != null) {
                                    intent.putParcelableArrayListExtra("emis", emis);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                                }
                                edittextDuration.setText("");
                                edittextPrincipal.setText("");
                            }
                        })
                        .negativeText(android.R.string.ok)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                edittextDuration.setText("");
                                edittextPrincipal.setText("");
                            }
                        })
                        .show();
            }
        });

    }

    private ArrayList<Emi> calcAllEmis(long principal, double rate, long duration) {
        ArrayList<Emi> emis = new ArrayList<>();
        if (duration < 1) {
            Toast.makeText(this, "Invalid duration", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (duration < 4 && duration > 0) {
            for (int i = 0; i <= duration + 3; i++) {
                emis.add(calcEmi(principal, rate, i));
            }
        } else {
            for (long i = duration - 4; i <= duration + 3; i++) {
                emis.add(calcEmi(principal, rate, i));
            }
        }
        return emis;
    }

    private Emi calcEmi(long principal, double rate, long duration) {
        Double Emi = principal * rate * (Math.pow(1 + rate, duration) / (Math.pow(1 + rate, duration) - 1));
        return new Emi(String.valueOf(principal), String.valueOf(duration),
                String.valueOf(Emi), String.valueOf(Emi * duration));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}