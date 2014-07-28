/**
 * Number 2048 Game
 * Author: Vasili Tsyvaniuk
 * e-mail: vasily802@gmail.com
 */

package blr.tsyvaniukvasili.numergame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener {
	
	TextView[][] tvarray;
	TextView cur_score, best_score, anim_score;
	TableLayout tv;
	boolean isWin, isWinShow = false;
	float x;
	float y,timediff=0;
	String sDown,playStyle;
	String sMove;
	String sUp;
	float xstart;
	float ystart;
	float xmove;
	float ymove;
	int[][] numarray;
	int resourceId,totalmoves=0,unprod=0;
	int current_score = 0;
	Date d = new Date();
	static final int PULL_RIGHT = 1;
	static final int PULL_LEFT = 2;
	static final int PULL_UP = 3;
	static final int PULL_DOWN = 4;
	static SharedPreferences spref;
	static final String SAVED_BEST = "saved_best";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this,DataService.class));
		cur_score = (TextView) findViewById(R.id.score);
		best_score = (TextView) findViewById(R.id.bestscore);
		anim_score = (TextView) findViewById(R.id.anim_score);
		tv = (TableLayout) findViewById(R.id.TableLayout01);
		tv.setOnTouchListener(this);
		tvarray = new TextView[4][4];
		numarray = new int[4][4];
		restart();

		// bloc for testing
		/**
		Scanner sc = null;
		try {
			sc = new Scanner(getAssets().open("table.txt"));
			while (sc.hasNextInt()) {
				for (int i = 0; i < 4; i++)
					for (int j = 0; j < 4; j++) {
						numarray[i][j] = sc.nextInt();
						if (numarray[i][j] != 0)
							tvarray[i][j].setText(Integer
									.toString(numarray[i][j]));
						else
							tvarray[i][j].setText("");
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
		**/
		setDraw();
		score(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "Leaderboard");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Intent intent = new Intent(this, Leaderboard.class);
		//startActivity(intent);
		return super.onOptionsItemSelected(item);
	}
	
	public void GenerateReport(){
	    int lc=0,rc=0,uc=0,dc=0,upm=0,diff1=0,diff2 = 0,diff3 =0;
	    int lastScore=0;
		BufferedReader br = null;
	       File sdCard = Environment.getExternalStorageDirectory();
	        //File directory = new File (sdCard.getAbsolutePath() + "/Data/sensorkmk.txt");
	        //if(!directory.exists())
	        //directory.mkdirs();
	        //String fname = "sensorkmk.txt";
	        //File file = new File (directory, fname);
	 		try {
	  
	 			String sCurrentLine;
	  
	 			br = new BufferedReader(new FileReader(sdCard.getAbsolutePath() + "/Data/sensorkmk.txt"));
	  
	 			while ((sCurrentLine = br.readLine()) != null) {
	 				
	 				 //Put p = new Put(Bytes.toBytes("row1"),timestamp);
	 				
	 				if(sCurrentLine.equals(""))
	 				{
	 					continue;
	 				}
	 				
	 				String[] array = sCurrentLine.split("\t");
	 				String move = array[0];
	 				if("1"==move){
	 					lc++;
	 				}
	 				else if("2"==move){
	 					rc++;
	 				}
	 				else if("3"==move){
	 					uc++;
	 				}
	 				else dc++;
	 				
	 				int sc=Integer.parseInt(array[1]);
	 				if(sc==lastScore) upm++;
	 				
	 				lastScore = sc;
	 				
	 				int diff = Integer.parseInt(array[3]);
	 				if(diff > diff3){
	 					diff1 = diff2;
	 					diff2 = diff3;
	 					diff3 = diff;
	 				}
	 			}
	 		
	 		}
	 		catch (Exception e) {
	               e.printStackTrace();
	        }
	 				
	 		totalmoves = lc+rc+uc+dc;
	 		timediff = (diff1+diff2+diff3)/3;
	 		unprod = upm;
	 		if((lc+rc)>=(uc+dc)){
	 			if(uc>=dc){
	 				playStyle="Left-Right with Top direction as base";
	 			}
	 			else playStyle="Left-Right with Bottom direction as base";
	 		}
	 		else{
	 			if(lc>=rc) playStyle="Left concentrated Up-Down direction";
	 			else playStyle="Right concentrated Up-Down direction";
	 		}
	 		// p.add(Bytes.toBytes("GeoLocation"), Bytes.toBytes("col"+count),Bytes.toBytes(latitude+","+longitude));
	 				  
	}
	
	public void SaveData(String string) {
	      // Log.i("string", string);
	        File sdCard = Environment.getExternalStorageDirectory();
	        File directory = new File (sdCard.getAbsolutePath() + "/Data");
	        if(!directory.exists())
	        directory.mkdirs();
	        String fname = "sensorkmk.txt";
	        File file = new File (directory, fname);
	        
	        try {
	            if(!file.exists())
	                file.createNewFile();
	               FileOutputStream out = new FileOutputStream(file,true);
	               out.write(string.getBytes());
	               out.flush();
	               out.close();

	        } catch (Exception e) {
	               e.printStackTrace();
	        }
	    }
	// method for calculate and move number (right, left, up, down)
	public void game(int move) {
		int animsc = 0;
		boolean isMoveOrCalc = false;
		String str;
		Date d1 = new Date();
		long diff = d1.getTime() - d.getTime();
		d = d1;		
		switch (move) {
		case PULL_RIGHT:
			str = "2"+"\t"+ current_score+"\t"+ d1 + "\t"+ diff/1000 +"\n";
			SaveData(str);
			for (int i = 0; i < 4; i++)
				for (int j = 3; j > 0; j--) {
					for (int k = j - 1; k >= 0; k--) {
						if (numarray[i][j] == 0)
							break;
						if ((numarray[i][j] != 0)
								&& (numarray[i][j] != numarray[i][k])
								&& (numarray[i][k] != 0))
							break;
						if ((numarray[i][j] == numarray[i][k])
								&& (numarray[i][j] != 0)) {
							isMoveOrCalc = true;
							numarray[i][j] = numarray[i][j] * 2;
							score(numarray[i][j]);
							animsc += numarray[i][j];
							if (numarray[i][j] == 2048)
								isWin = true;
							numarray[i][k] = 0;
							break;
						}

					}
				}
			for (int i = 0; i < 4; i++)
				for (int k = 0; k < 3; k++)
					for (int j = 0; j < 3; j++) {
						if ((numarray[i][j] != 0) && (numarray[i][j + 1] == 0)) {
							numarray[i][j + 1] = numarray[i][j];
							numarray[i][j] = 0;
							isMoveOrCalc = true;
						}
					}
			break;

		case PULL_LEFT:
			 str = "1"+"\t"+ current_score+"\t"+ d1 + "\t"+ diff/1000 +"\n";
			SaveData(str);
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 3; j++) {
					for (int k = j + 1; k < 4; k++) {
						if (numarray[i][j] == 0)
							break;
						if ((numarray[i][j] != 0)
								&& (numarray[i][j] != numarray[i][k])
								&& (numarray[i][k] != 0))
							break;
						if ((numarray[i][j] == numarray[i][k])
								&& (numarray[i][j] != 0)) {
							isMoveOrCalc = true;
							numarray[i][j] = numarray[i][j] * 2;
							score(numarray[i][j]);
							animsc += numarray[i][j];
							if (numarray[i][j] == 2048)
								isWin = true;
							numarray[i][k] = 0;
							break;
						}

					}

				}
			for (int i = 0; i < 4; i++)
				for (int k = 0; k < 3; k++)
					for (int j = 3; j > 0; j--) {
						if ((numarray[i][j] != 0) && (numarray[i][j - 1] == 0)) {
							numarray[i][j - 1] = numarray[i][j];
							numarray[i][j] = 0;
							isMoveOrCalc = true;
						}
					}
			break;

		case PULL_DOWN:
			 str = "4"+"\t"+ current_score+"\t"+ d1 + "\t"+ diff/1000 +"\n";
			SaveData(str);
			for (int i = 0; i < 4; i++)
				for (int j = 3; j > 0; j--) {
					for (int k = j - 1; k >= 0; k--) {
						if (numarray[j][i] == 0)
							break;
						if ((numarray[j][i] != 0)
								&& (numarray[j][i] != numarray[k][i])
								&& (numarray[k][i] != 0))
							break;
						if ((numarray[j][i] == numarray[k][i])
								&& (numarray[j][i] != 0)) {
							isMoveOrCalc = true;
							numarray[j][i] = numarray[j][i] * 2;
							score(numarray[j][i]);
							animsc += numarray[j][i];
							if (numarray[j][i] == 2048)
								isWin = true;
							numarray[k][i] = 0;
							break;
						}

					}

				}
			for (int i = 0; i < 4; i++)
				for (int k = 0; k < 3; k++)
					for (int j = 0; j < 3; j++) {
						if ((numarray[j][i] != 0) && (numarray[j + 1][i] == 0)) {
							numarray[j + 1][i] = numarray[j][i];
							numarray[j][i] = 0;
							isMoveOrCalc = true;
						}
					}
			break;

		case PULL_UP:
			str = "\t"+ current_score+"\t"+ d1 + "\t"+ diff/1000 +"\n";
			SaveData(str);
			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 3; j++) {
					for (int k = j + 1; k < 4; k++) {
						if (numarray[j][i] == 0)
							break;
						if ((numarray[j][i] != 0)
								&& (numarray[j][i] != numarray[k][i])
								&& (numarray[k][i] != 0))
							break;
						if ((numarray[j][i] == numarray[k][i])
								&& (numarray[j][i] != 0)) {
							isMoveOrCalc = true;
							numarray[j][i] = numarray[j][i] * 2;
							score(numarray[j][i]);
							animsc += numarray[j][i];
							if (numarray[j][i] == 2048)
								isWin = true;
							numarray[k][i] = 0;
							break;
						}

					}

				}
			for (int i = 0; i < 4; i++)
				for (int k = 0; k < 3; k++)
					for (int j = 3; j > 0; j--) {
						if ((numarray[j][i] != 0) && (numarray[j - 1][i] == 0)) {
							numarray[j - 1][i] = numarray[j][i];
							numarray[j][i] = 0;
							isMoveOrCalc = true;
						}
					}
			break;

		}
		if ((isMoveOrCalc) || (isFill()))
			myRandom();
		if (animsc != 0)
			startAnim(animsc);
		setDraw();

	}

	// set color to View
	public void setDraw() {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				resourceId = getResources().getIdentifier(
						"tvshape" + numarray[i][j], "drawable",
						getPackageName());
				if (numarray[i][j] != 0) {
					tvarray[i][j].setText(Integer.toString(numarray[i][j]));
					tvarray[i][j].setBackgroundResource(resourceId);
					tvarray[i][j].setTextColor(getResources().getColor(
							R.color.textgray));
					if (numarray[i][j] > 4)
						tvarray[i][j].setTextColor(getResources().getColor(
								R.color.textwhite));
				} else {
					tvarray[i][j].setText("");
					tvarray[i][j].setBackgroundResource(R.drawable.tvshape);
				}
			}
	}

	// Generate random for number 2
	public void myRandom() {
		ArrayList<Integer> rows = new ArrayList<Integer>();
		ArrayList<Integer> columns = new ArrayList<Integer>();
		boolean isEmpty = false;
		if ((isWin) && (!isWinShow)) {
			gameWinDialog();
			isWinShow = true;
		}
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				if (numarray[i][j] == 0) {
					rows.add(i);
					columns.add(j);
					isEmpty = true;
				}
			}

		if (isEmpty) {
			int randnumber = (int) (Math.random() * rows.size());
			int randrow = rows.get(randnumber);
			int randcolomn = columns.get(randnumber);
			numarray[randrow][randcolomn] = 2;
			tvarray[randrow][randcolomn].setText("2");
			Animation anim = null;
			tvarray[randrow][randcolomn]
					.setBackgroundResource(R.drawable.tvshape2);
			anim = AnimationUtils.loadAnimation(this, R.anim.scale_random);
			tvarray[randrow][randcolomn].startAnimation(anim);

		} else {
			boolean isGameOver = true;
			for (int i = 1; i < 4; i++)
				for (int j = 0; j < 3; j++)
					if ((numarray[i][j] == numarray[i][j + 1])
							|| (numarray[i][j] == numarray[i - 1][j])
							|| (numarray[i - 1][j] == numarray[i - 1][j + 1])
							|| (numarray[i - 1][j + 1] == numarray[i][j + 1]))
						isGameOver = false;
			if (isGameOver) {
				gameOverDialog();
			}
		}

	}

	// Check empty tile
	public boolean isFill() {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				if (numarray[i][j] == 0)
					return false;
		return true;
	}

	// Restarting game
	public void restart() {
		isWinShow = isWin = false;
		current_score = 0;
		score(0);
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				resourceId = getResources().getIdentifier("tv" + i + j, "id",
						getPackageName());
				tvarray[i][j] = (TextView) findViewById(resourceId);
				numarray[i][j] = 0;
			}
		myRandom();
		setDraw();
	}

	// Calculate score and best score
	public void score(int sc) {
		current_score += sc;
		Resources res = getResources();
		String text = String.format(res.getString(R.string.score),
				current_score);
		cur_score.setText(text);
		spref = getPreferences(MODE_PRIVATE);
		String saved_best = spref.getString(SAVED_BEST, "0");
		if (current_score > Integer.parseInt(saved_best)) {
			saved_best = Integer.toString(current_score);
			Editor ed = spref.edit();
			ed.putString(SAVED_BEST, saved_best);
			ed.commit();
		}
		best_score.setText(String.format(res.getString(R.string.bestscore),
				saved_best));

	}

	// Show Game over to user
	public void gameOverDialog() {
		GenerateReport();
		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.gameover_dialog);
		TextView score = (TextView) dialog.findViewById(R.id.gover_score);
		Resources res = getResources();
		CharSequence text = bold(
				res.getString(R.string.your_score),
				" ",
				color(res.getColor(R.color.textorange),
						Integer.toString(current_score)), " ",
				res.getString(R.string.points));
		score.setText(text + "\n" + "Total moves " + totalmoves + "\n" + "Unscored moves "+unprod+"\n"+"Max response time " + timediff + "\n"+"Your Game plan style: "+playStyle);
		dialog.show();
		Button btn = (Button) dialog.findViewById(R.id.try_again_button);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				restart();
				dialog.dismiss();
			}
		});

	}

	// Show Win to user
	public void gameWinDialog() {
		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.win_dialog);
		TextView score = (TextView) dialog.findViewById(R.id.win_score);
		Resources res = getResources();
		CharSequence text = bold(
				res.getString(R.string.your_score),
				" ",
				color(res.getColor(R.color.textorange),
						Integer.toString(current_score)), " ",
				res.getString(R.string.points));
		score.setText(text);
		dialog.show();
		Button btn = (Button) dialog.findViewById(R.id.go_on_button);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

	}

	// Show Restart Dialog to user
	public void restartDialog() {
		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar);
		dialog.setContentView(R.layout.restart_dialog);
		dialog.show();
		Button btn_yes = (Button) dialog.findViewById(R.id.yes_button);
		Button btn_no = (Button) dialog.findViewById(R.id.no_button);
		btn_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				restart();
				dialog.dismiss();
			}
		});
		btn_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	// Handling touches
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		x = event.getX();
		y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xstart = x;
			ystart = y;
			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			xmove = x - xstart;
			ymove = y - ystart;
			if ((xmove > 0) && (Math.abs(xmove) > Math.abs(ymove)))
				game(PULL_RIGHT);
			if ((xmove < 0) && (Math.abs(xmove) > Math.abs(ymove)))
				game(PULL_LEFT);
			if ((ymove < 0) && (Math.abs(xmove) < Math.abs(ymove)))
				game(PULL_UP);
			if ((ymove > 0) && (Math.abs(xmove) < Math.abs(ymove)))
				game(PULL_DOWN);

			break;
		}

		return true;
	}

	public void startAnim(int anscore) {
		Animation anim = null;
		anim_score.setVisibility(View.VISIBLE);
		anim_score.setText("+" + Integer.toString(anscore));
		anim = AnimationUtils.loadAnimation(this, R.anim.trans_score);
		anim_score.startAnimation(anim);
		anim_score.setVisibility(View.INVISIBLE);
	}

	// Click Leaderboard in menu
	public void onMenuClick(View v) {
		openOptionsMenu();
	}

	// Click to restart button
	public void restartClick(View v) {
		restartDialog();
	}

	// Methods for styling text in views
	public static CharSequence color(int color, CharSequence... content) {
		return apply(content, new ForegroundColorSpan(color));
	}

	public static CharSequence bold(CharSequence... content) {
		return apply(content, new StyleSpan(Typeface.BOLD));
	}

	private static CharSequence apply(CharSequence[] content, Object... tags) {
		SpannableStringBuilder text = new SpannableStringBuilder();
		openTags(text, tags);
		for (CharSequence item : content) {
			text.append(item);
		}
		closeTags(text, tags);
		return text;
	}

	private static void openTags(Spannable text, Object[] tags) {
		for (Object tag : tags) {
			text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
		}
	}

	private static void closeTags(Spannable text, Object[] tags) {
		int len = text.length();
		for (Object tag : tags) {
			if (len > 0) {
				text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				text.removeSpan(tag);
			}
		}
	}
	@Override
	protected void onResume() {
	//	Gameview.resume();

		super.onResume();	
		Log.i("data in main class", "gghg");
		registerReceiver(receiver, new IntentFilter("myproject"));
		
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			
			if (bundle!=null) {
				
				String data1 = bundle.getString("data");
				Log.i("data in main class", "hey yup");
				
				
				if ("stomp".equalsIgnoreCase(data1)) {
					
					Log.i("stomp", "moving down");
					game(PULL_DOWN);
					//view.swipeDown();
				}	
				else if ("fly".equalsIgnoreCase(data1)) {
					
					Log.i("fly", "moving up");
					game(PULL_UP);
					//view.swipeUp();
					
				}
				else if ("left".equalsIgnoreCase(data1)) {
					Log.i("left", "moving left");
					game(PULL_LEFT);
					//swipeLeft();
				
				}
				else if ("right".equalsIgnoreCase(data1)) {
					
					Log.i("right", "moving right");
					//view.swipeRight();
					game(PULL_RIGHT);
					
					
				}
				//Toast.makeText(getApplicationContext(), "Ok", Toast.LENGTH_SHORT).show();
			}else{
				Log.i("data in main class", "bundle null");
				//Toast.makeText(getApplicationContext(), "not", Toast.LENGTH_SHORT).show();
			}
			//handleResult(bundle);*/
		}

		
	};
	@Override
	protected void onPause() {
		//view.pause();
		super.onPause();
		 unregisterReceiver(receiver);
		
	}

}