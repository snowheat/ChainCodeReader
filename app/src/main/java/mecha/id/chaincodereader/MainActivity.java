package mecha.id.chaincodereader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static mecha.id.chaincodereader.R.id.constraintLayout;
import static mecha.id.chaincodereader.R.id.predictTheCharacterButton;

public class MainActivity extends AppCompatActivity {

    private static String LOG = "WOW";
    private TextView predictedCharacterTextView,chainCodeTextView;
    private String predictedCharacter,chainCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        predictedCharacterTextView = (TextView) findViewById(R.id.predictedCharacterTextView);
        chainCodeTextView = (TextView) findViewById(R.id.chainCodeTextView);

        final PixelGridView gridView = (PixelGridView) new PixelGridView(this);
        gridView.setNumColumns(24);
        gridView.setNumRows(18);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.addView(gridView);

        Button clearGridButton = (Button) findViewById(R.id.clearGridButton);
        clearGridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridView.clearGrid();
                predictedCharacterTextView.setText(null);
                chainCodeTextView.setText(null);
            }
        });

        Button predictTheCharacterButton = (Button) findViewById(R.id.predictTheCharacterButton);
        predictTheCharacterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    chainCode = gridView.getChainCode();
                    chainCodeTextView.setText(chainCode);

                    predictedCharacter = gridView.getPredictedCharacter(chainCode);
                    Log.i(LOG,"makan nasi "+predictedCharacter);
                    predictedCharacterTextView.setText(predictedCharacter);
                }catch(Exception e){
                    Log.e(LOG,e.getMessage());
                }
            }
        });





    }
}
