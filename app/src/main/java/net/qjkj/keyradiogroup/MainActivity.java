package net.qjkj.keyradiogroup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.qjkj.keyradiogroup.widget.KeyRadioGroupV1;
import net.qjkj.keyradiogroup.widget.KeyRadioGroupV2;

public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private KeyRadioGroupV1 mKeyRadioGroupV1;
    private Button mButton2;
    private KeyRadioGroupV2 mKeyRadioGroupV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //V1 测试
        mKeyRadioGroupV1 = (KeyRadioGroupV1) findViewById(R.id.krg_main_1);
        mButton = (Button) findViewById(R.id.b_main);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "current: " + mKeyRadioGroupV1.getCheckedRadioButtonId(), Toast.LENGTH_SHORT).show();
            }
        });

        //V2 test
        mKeyRadioGroupV2 = (KeyRadioGroupV2) findViewById(R.id.krg_main_2);
        mButton2 = (Button) findViewById(R.id.b_main_2);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "current: " + mKeyRadioGroupV2.getCheckedRadioButtonId(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
