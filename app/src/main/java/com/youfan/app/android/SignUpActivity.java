package com.youfan.app.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.youfan.app.android.data.SignUpParams;
import com.youfan.app.android.data.SignUpResponse;
import com.youfan.app.android.data.UserInfo;
import com.youfan.app.android.service.YouFanServices;
import com.facebook.login.LoginManager;

import rx.Observer;

public class SignUpActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        final EditText userBio = (EditText)findViewById(R.id.enter_bio);
        Button signUpDoneButton = (Button)findViewById(R.id.sign_up_done_button);

        signUpDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bio = userBio.getText().toString();
                if (bio != null && !bio.isEmpty()) {
                    Db.getUser().userInfo.bio = bio;
                    signup();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.self_introduction), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signup() {
        UserInfo userInfo = Db.getUser().userInfo;

        final SignUpParams signUpParams = new SignUpParams(userInfo.firstName, userInfo.lastName, userInfo.bio,
            Db.getUser().facebookId, Db.getUser().facebookToken, Db.getUserPhotoFile());

        YouFanServices.getInstance().signUp(signUpParams, new Observer<SignUpResponse>() {
            @Override
            public void onCompleted() {
                finish();
            }

            @Override
            public void onError(Throwable e) {
                Log.d("sign up", "error");
            }

            @Override
            public void onNext(SignUpResponse signUpResponse) {
                Log.d("sign up", "success");

                //set userInfo, Token and YouFan user_id in Db
                Db.setYouFanToken(signUpResponse.token);
                Db.getUser().userInfo = signUpResponse.user;
                Db.setUserId(signUpResponse.user.id);

                //save facebookId to disk
                Db.saveFacebookIdToDisk(getApplicationContext(), Db.getFacebookId());

                //save to disk: userInfo(first name, last name, user bio, photoUrl),  YouFanToken
                Db.saveUserAndYouFanTokenToDisk(getApplicationContext());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LoginManager.getInstance().logOut();
        Db.logOutClear(getApplicationContext());
    }
}
