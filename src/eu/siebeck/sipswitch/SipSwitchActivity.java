package eu.siebeck.sipswitch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * @author Robert G. Siebeck <robert@siebeck.org>
 *
 */
public class SipSwitchActivity extends AppWidgetProvider {
	private static final String 
	CALL_MODE = "eu.siebeck.sipswitch.CALL_MODE",
		EXTRA_CALL_MODE = "eu.siebeck.sipswitch.EXTRA_CALL_MODE";
	private static final String LOG = SipSwitchActivity.class.getName();

	private static final String
		SIP_CALL_OPTIONS = "sip_call_options",
		SIP_ALWAYS = "SIP_ALWAYS",
		SIP_ADDRESS_ONLY = "SIP_ADDRESS_ONLY",
		SIP_ASK_ME_EACH_TIME = "SIP_ASK_ME_EACH_TIME";

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] widgetIds) {
//		Debug.waitForDebugger();

		final String callMode = Settings.System.getString(
					context.getContentResolver(),
					SIP_CALL_OPTIONS);
		if (callMode == null) {
			Log.w(LOG, "SIP_CALL_OPTIONS was null");
			setCallMode(context, SIP_ASK_ME_EACH_TIME);
		}

		final RemoteViews views = new RemoteViews(
				context.getApplicationContext().getPackageName(),
				R.layout.widget_layout);

		views.setImageViewResource(R.id.ind_mode, getModeIndicator(callMode));
		views.setImageViewResource(R.id.img_mode, getModeImage(callMode));

		final Intent callModeClickIntent = new Intent(context, SipSwitchActivity.class);
		callModeClickIntent.setAction(CALL_MODE);
		callModeClickIntent.putExtra(EXTRA_CALL_MODE, callMode);

		final PendingIntent pendingCallModeClickIntent = PendingIntent
				.getBroadcast(context, 0, callModeClickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.callModeButton,
				pendingCallModeClickIntent);

		for (final int widgetId : widgetIds) {
			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		if (CALL_MODE.equals(action)) {
	//		Debug.waitForDebugger();
			final String callMode = toggleCallMode(intent.getStringExtra(EXTRA_CALL_MODE));
			setCallMode(context, callMode);
	
			updateWidgetView(context);
	
			Toast.makeText(context, getModeToast(callMode), Toast.LENGTH_SHORT).show();
		}
		super.onReceive(context, intent);
	}

	private void setCallMode(final Context context, final String callMode) {
		Log.i(LOG, "Setting callMode to " + callMode);
		Settings.System.putString(context.getContentResolver(),
				SIP_CALL_OPTIONS, callMode);
	}

	private void updateWidgetView(final Context context) {
		final AppWidgetManager appWidgetManager =
				AppWidgetManager.getInstance(context.getApplicationContext());
		final ComponentName thisWidget = new ComponentName(context,
				SipSwitchActivity.class);
		final int[] widgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		final Intent update = new Intent(context, SipSwitchActivity.class);
		update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		context.sendBroadcast(update);
	}

	private int getModeToast(final String callMode) {
		if (SIP_ASK_ME_EACH_TIME.equals(callMode))
			return R.string.mode_ask;
		else if (SIP_ADDRESS_ONLY.equals(callMode))
			return R.string.mode_phone;
		else
			return R.string.mode_sip;
	}

	private String toggleCallMode(final String callMode) {
		if (SIP_ASK_ME_EACH_TIME.equals(callMode))
			return SIP_ADDRESS_ONLY;
		else if (SIP_ADDRESS_ONLY.equals(callMode))
			return SIP_ALWAYS;
		else
			return SIP_ASK_ME_EACH_TIME;
	}

	private int getModeIndicator(final String callMode) {
		if (SIP_ASK_ME_EACH_TIME.equals(callMode))
			return R.drawable.appwidget_settings_ind_mid_r;
		else if (SIP_ADDRESS_ONLY.equals(callMode))
			return R.drawable.appwidget_settings_ind_off_r;
		else
			return R.drawable.appwidget_settings_ind_on_r;
	}

	private int getModeImage(final String callMode) {
		if (SIP_ASK_ME_EACH_TIME.equals(callMode))
			return R.drawable.mode_ask;
		else if (SIP_ADDRESS_ONLY.equals(callMode))
			return R.drawable.mode_phone;
		else
			return R.drawable.mode_sip;
	}
}