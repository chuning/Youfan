package com.youfan.app.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.youfan.app.android.data.CheckUserExistParams;
import com.youfan.app.android.data.CheckUserExistResponse;
import com.youfan.app.android.data.User;
import com.youfan.app.android.data.UserInfo;
import com.youfan.app.android.service.YouFanServices;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import rx.Observer;

public class LoginActivity extends Activity {
	private LoginButton loginButton;
	private CallbackManager callbackManager;
	private ProfileTracker profileTracker;
	private AccessTokenTracker accessTokenTracker;
	private AccessToken accessToken;

	private static final String WECHAT_APP_ID = "wx0e1452305a680745";
	public static IWXAPI wechatApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_in_activity_dialog);

//		registerWeChat();
		callbackManager = CallbackManager.Factory.create();

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setReadPermissions("user_friends");

		// If the access token is available already assign it.
		accessToken = AccessToken.getCurrentAccessToken();

		final String dir = this.getCacheDir().toString();

		profileTracker = new ProfileTracker() {
			@Override
			protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
				User user = Db.getUser();
				if (user != null && Profile.getCurrentProfile()!=null) {
					String firstName = Profile.getCurrentProfile().getFirstName();
					String lastName = Profile.getCurrentProfile().getLastName();
					String photoUrl = Profile.getCurrentProfile().getProfilePictureUri(50, 50).toString();

					user.userInfo = new UserInfo(photoUrl, firstName, lastName);

					Picasso.with(getApplicationContext())
						.load(Uri.parse(user.userInfo.photoUrl))
						.into(new Target() {
							@Override
							public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
								Db.saveBitmapFile(bitmap, dir);
							}

							@Override
							public void onBitmapFailed(Drawable errorDrawable) {

							}

							@Override
							public void onPrepareLoad(Drawable placeHolderDrawable) {

							}
						});

					CheckUserExistParams checkUserExistParams = new CheckUserExistParams(user.facebookId, user.facebookToken);
					YouFanServices.getInstance().checkUserExist(checkUserExistParams, new Observer<CheckUserExistResponse>() {
						@Override
						public void onCompleted() {
							Log.d("chuning", "check exist user complete");
						}

						@Override
						public void onError(Throwable e) {
							Log.d("chuning", "check exist user error");
						}

						@Override
						public void onNext(CheckUserExistResponse checkUserExistResponse) {
							//if user not existed, call sign up activity
							if (!checkUserExistResponse.isExistingUser) {
								Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
								startActivity(intent);
								finish();
							}
						}
					});
				}
			}
		};

		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				String facebookId = loginResult.getAccessToken().getUserId();
				String facebookToken = loginResult.getAccessToken().getToken();
				User user = new User(facebookToken, facebookId);
				Db.setUser(user);
				Db.setFacebookId(facebookId);
				setResult(Activity.RESULT_OK);
			}

			@Override
			public void onCancel() {
				finish();
			}

			@Override
			public void onError(FacebookException exception) {
				//TODO: facebook log in error
			}
		});

//		//use wechat log in
//		Button weChatLoginButton = (Button) findViewById(R.id.wechat_login_button);
//
//		weChatLoginButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// send oauth request
//				SendAuth.Req req = new SendAuth.Req();
//				req.scope = "snsapi_userinfo";
//				req.state = "wechat_sdk_demo_test";
//				wechatApi.sendReq(req);
//			}
//		});

	}

	private void registerWeChat() {
		wechatApi = WXAPIFactory.createWXAPI(this, WECHAT_APP_ID, true);
		wechatApi.registerApp(WECHAT_APP_ID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		profileTracker.stopTracking();
	}
}

