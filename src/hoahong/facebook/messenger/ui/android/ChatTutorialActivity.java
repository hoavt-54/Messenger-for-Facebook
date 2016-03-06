package hoahong.facebook.messenger.ui.android;

import hoahong.facebook.messenger.R;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ChatTutorialActivity extends AppCompatActivity implements OnClickListener{
	private ImageView imageView;
	private Button finishedButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_tutorial_layout);
		imageView = (ImageView) findViewById(R.id.chat_tutorial_image_view);
		finishedButton = (Button) findViewById(R.id.chat_tutorial_finished_btn);
		imageView.setOnClickListener(this);
		finishedButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		setResult(RESULT_OK);
		finish();
	}
}
