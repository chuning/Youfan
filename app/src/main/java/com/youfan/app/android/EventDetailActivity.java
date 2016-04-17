package com.youfan.app.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.youfan.app.android.data.AttendEventParams;
import com.youfan.app.android.data.AttendEventResponse;
import com.youfan.app.android.data.AttendUserResponse;
import com.youfan.app.android.data.CommentResponse;
import com.youfan.app.android.data.UserInfo;
import com.youfan.app.android.service.YouFanServices;
import com.youfan.app.android.util.AppUtil;
import com.youfan.app.android.util.Constants;

import rx.Observer;
import rx.subjects.PublishSubject;

public class EventDetailActivity extends AppCompatActivity{
	private PageAdapter mPageAdapter;
	private ViewPager mViewPager;
	private String eventId;
	private TabLayout tabLayout;
	private TextView joinButton;
	private boolean isJoined;
	private boolean isEventFull;
	private PublishSubject<Boolean> commentsObservable = PublishSubject.create();
	private PublishSubject<Boolean> attendUsersObservable = PublishSubject.create();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail_activity);
		joinButton = (TextView) findViewById(R.id.done_button);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		tabLayout = (TabLayout)findViewById(R.id.tab_layout);
		tabLayout.addTab(tabLayout.newTab().setText(R.string.detail));

		tabLayout.addTab(tabLayout.newTab().setText(R.string.discuss));

		mPageAdapter = new PageAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPageAdapter);
		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				if (position == 1) {
					commentsObservable.onNext(true);
				}
			}
		});

		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				if (tab.getPosition() == 1) {
					commentsObservable.onNext(true);
				}
			}
		});

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			eventId = extras.getString(Constants.EVENT_ID);
		}

		commentsObservable.subscribe(commentsObserver());
		commentsObservable.onNext(true);

		attendUsersObservable.subscribe(attendUserObserver());
		attendUsersObservable.onNext(true);

		if (!Db.getEvent().hostedBy.id.equals(Db.getUserId())) {
			joinButton.setVisibility(View.VISIBLE);
		} else {
			joinButton.setVisibility(View.GONE);
		}

		joinButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!AppUtil.isLogin()) {
					Intent intent = new Intent(v.getContext(), LoginActivity.class);
					startActivityForResult(intent, Constants.CHECK_USER_EXIST_REQUEST_CODE);
				}
				else if (isEventFull) {
					Toast toast = Toast.makeText(getApplicationContext(),"Oh no...\n这个饭局暂时满员啦T T", Toast.LENGTH_SHORT);
					toast.show();
				}
				else if (!isJoined) {
					attendEvent();
					Toast toast = Toast.makeText(getApplicationContext(), "报名成功", Toast.LENGTH_SHORT);
					toast.show();
				}
				else {
					quitEvent();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void attendEvent() {
		isJoined = true;
		joinButton.setText(R.string.quit_event);
		String token = Db.getYouFanToken();
		AttendEventParams params = new AttendEventParams(Db.getUserId(), eventId);
		YouFanServices.getInstance().attendEvent(token, params, attendEventResponseObserver());
	}

	public void quitEvent() {
		new AlertDialog.Builder(this)
			.setTitle("退出饭局")
			.setMessage("亲，确定要退出这次饭局吗?")
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// continue with delete
					isJoined = false;
					joinButton.setText(R.string.want_to_join);
					String token = Db.getYouFanToken();
					AttendEventParams params = new AttendEventParams(Db.getUserId(), eventId);
					YouFanServices.getInstance().quitEvent(token, params, quitEventResponseObserver());
					Toast toast = Toast.makeText(getApplicationContext(), "已退出", Toast.LENGTH_SHORT);
					toast.show();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// do nothing
				}
			})
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
	}

	public static class PageAdapter extends FragmentPagerAdapter {

		public PageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new EventDetailFragment();
			case 1:
				return new EventCommentFragment();
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Section " + (position + 1);
		}
	}

	private Observer<Boolean> commentsObserver() {
		return new Observer<Boolean>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
				Log.d("error", e.getMessage());
			}

			@Override
			public void onNext(Boolean isSearch) {
				YouFanServices.getInstance().getComment(eventId, commentsLoadedObserver());
			}
		};
	}

	private Observer<CommentResponse> commentsLoadedObserver() {
		return new Observer<CommentResponse>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
				Log.d("error", e.getMessage());
			}

			@Override
			public void onNext(CommentResponse commentResponse) {
				Fragment commentFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":1");

				if (commentFragment != null) {
					((EventCommentFragment)commentFragment).getCommentResultsObservable().onNext(commentResponse.results);
					tabLayout.getTabAt(1).setText(getResources().getString(R.string.discuss_template, commentResponse.results.size()));
				}
			}
		};
	}

	public Observer<AttendEventResponse> attendEventResponseObserver() {
		return new Observer<AttendEventResponse>() {
			@Override
			public void onCompleted() {
				Log.d("event attend:", "complete");
			}

			@Override
			public void onError(Throwable e) {
				Log.d("event attend:", "error");
			}

			@Override
			public void onNext(AttendEventResponse eventResponse) {
				attendUsersObservable.onNext(true);
			}
		};
	}

	public Observer<AttendEventResponse> quitEventResponseObserver() {
		return new Observer<AttendEventResponse>() {
			@Override
			public void onCompleted() {
				Log.d("event unattend:", "complete");
			}

			@Override
			public void onError(Throwable e) {
				Log.d("event unattend:", "error");
			}

			@Override
			public void onNext(AttendEventResponse eventResponse) {
				Log.d("event unattend:", "next");
				attendUsersObservable.onNext(true);
			}
		};
	}

	private Observer<Boolean> attendUserObserver() {
		return new Observer<Boolean>() {
			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onNext(Boolean isSearch) {
				YouFanServices.getInstance().getAttendUsers(eventId, attendeeLoadedObserver());
			}
		};
	}

	private Observer<AttendUserResponse> attendeeLoadedObserver() {
		return new Observer<AttendUserResponse>() {
			@Override
			public void onCompleted() {
				Log.d("attendeeLoaded observer", "complete");
			}

			@Override
			public void onError(Throwable e) {
				Log.d("attendeeLoaded observer", "error");
			}

			@Override
			public void onNext(AttendUserResponse attendUserResponse) {
				isJoined = false;
				for (UserInfo attendee: attendUserResponse.results) {
					//check if the user has already joined
					if (attendee.id.equals(Db.getUserId())) {
						isJoined = true;
					}
				}
				setJoinButton(isJoined);

				//check if event is full
				if (attendUserResponse.results.size() + 1 >= Db.getEvent().quota) {
					isEventFull = true;
				} else {
					isEventFull = false;
				}
				Fragment detailFragment = getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
				if (detailFragment != null) {
					((EventDetailFragment)detailFragment).getAttendUserObservable().onNext(attendUserResponse.results);
				}
			}
		};
	}

	private void setJoinButton(Boolean isJoined) {
		if (!isJoined) {
			joinButton.setText(R.string.want_to_join);
		} else {
			joinButton.setText(R.string.quit_event);
		}
	}
}

