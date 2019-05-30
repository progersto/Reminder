package com.start.reminder;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.start.model.User;
import com.start.utils.PreferUtilKt;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private ImageView interceptedNotificationImageView;
    private AlertDialog enableNotificationListenerAlertDialog;
    private Button permissoinBtn, repeatBtn;
    private TextView time;
    private SeekBar seekBar;
    private LiveData<ValueliveData> liveData;
    private LiveData<String> liveDataS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        interceptedNotificationImageView = findViewById(R.id.intercepted_notification_logo);
        permissoinBtn = findViewById(R.id.permissoinBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        time = findViewById(R.id.time);
        seekBar = findViewById(R.id.seekBar);

        if (PreferUtilKt.restoreTime(this) == 0) {
            PreferUtilKt.saveTime(this, Integer.parseInt(time.getText().toString()));
        } else {
            time.setText(String.valueOf(PreferUtilKt.restoreTime(this)));
        }

        if (PreferUtilKt.isCanselAlarm(this)) {
            repeatBtn.setActivated(true);
            repeatBtn.setText("Остановить уведомления");
        } else {
            repeatBtn.setText("Нет уведомлений");
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                }
                time.setText(String.valueOf(progress));
                PreferUtilKt.saveTime(seekBar.getContext(), progress);
                if (PreferUtilKt.isCanselAlarm(seekBar.getContext())) {
                    Alarm.Companion.cancelAlarm(MainActivity.this);
                    Alarm.Companion.setAlarm(MainActivity.this);
                    Log.d("Package__", "Alarm is reinstalled");
                } else Log.d("Package__", "set time");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBtn.getText().toString().equals("Нет уведомлений")) {
                    Toast.makeText(v.getContext(), "Нет уведомлений", Toast.LENGTH_SHORT).show();
                } else {
                    Alarm.Companion.cancelAlarm(v.getContext());
                    repeatBtn.setText("Нет уведомлений");
                }
            }
        });
        permissoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });

        if (!isNotificationServiceEnabled()) {
            permissoinBtn.setText("нет разрешений");
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        } else {
            permissoinBtn.setText("есть разрешения");
        }

        liveData = DataController.Companion.getInstance().getLifeData();
        liveData.observe(this, new Observer<ValueliveData>() {
            @Override
            public void onChanged(ValueliveData value) {
                Log.d("Package__", "liveData Observe");
                setTextBtn(value.getAction());//stop notification text
                changeInterceptedNotificationImage(value.getNotificationCode());
            }
        });
        liveDataS = DataController.Companion.getInstance().getLifeDataS();
        liveDataS.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
               setTextPermission(s);
            }
        });

        DataController.Companion.getInstance().getLiveDataUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                Log.d("Package__", user.toString());
            }
        });
    }//onCreate

    private void changeInterceptedNotificationImage(int notificationCode) {
        switch (notificationCode) {
            case NotificationListenerExampleService.WHATSAPP_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.whatsapp_logo);
                break;
            case NotificationListenerExampleService.TELEGRAM_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.telegram);
                break;
            case NotificationListenerExampleService.CALL_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.call);
                break;
            case NotificationListenerExampleService.OTHER_NOTIFICATIONS_CODE:
                interceptedNotificationImageView.setImageResource(R.drawable.notif_icon_2_no);
                break;
        }
    }

    private void setTextBtn(boolean send) {
        if (send) {
            repeatBtn.setText("Остановить уведомления");
        } else {
            repeatBtn.setText("Нет уведомлений");
        }
    }

    private void setTextPermission(String s) {
        switch (s) {
            case "onBind":
                permissoinBtn.setText("есть разрешения");
                break;
            case "onUnbind":
                permissoinBtn.setText("нет разрешений");
                break;
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("Package__", "NegativeButton");
                    }
                });
        return (alertDialogBuilder.create());
    }
}

