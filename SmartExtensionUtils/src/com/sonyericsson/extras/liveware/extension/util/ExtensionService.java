/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2012 Sony Mobile Communications AB.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sonyericsson.extras.liveware.extension.util;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.Device;
import com.sonyericsson.extras.liveware.aef.registration.Registration.DeviceColumns;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;
import com.sonyericsson.extras.liveware.extension.util.registration.IRegisterCallback;
import com.sonyericsson.extras.liveware.extension.util.registration.RegisterExtensionTask;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import java.util.HashMap;

/**
 * The extension service is an abstract class that should be extended for
 * accessory extensions.
 */
public abstract class ExtensionService extends Service implements IRegisterCallback {

  private class IntentRunner implements Runnable {
    protected Intent mIntent;
    protected int mRunnerStartId;

    IntentRunner(Intent intent, int startId) {
      mIntent = intent;
      mRunnerStartId = startId;
    }

    /**
     * Does nothing should be overridden
     */
    @Override
    public void run() {
    }
  }

  public static final int INVALID_ID = -1;

  private RegisterExtensionTask mRegisterTask = null;

  private final String mExtensionKey;

  private RegistrationInformation mRegistrationInformation;

  private HashMap<String, ControlExtension> mControls = new HashMap<String, ControlExtension>();

  private int mStartId;

  private Handler mHandler;

  private boolean mPendingNewRegistration = false;

  private boolean mUpdateSourceRegistration = true;

  /**
   * Create instance of ExtensionService
   *
   * @param extensionKey The extension key.
   */
  public ExtensionService(String extensionKey) {
    if (extensionKey == null) {
      throw new IllegalArgumentException("extensionKey == null");
    }
    mExtensionKey = extensionKey;
  }

  /**
   * {@inheritDoc}
   *
   * @see android.app.Service#onCreate()
   */
  @Override
  public void onCreate() {
    super.onCreate();

    // Get registration information
    mRegistrationInformation = getRegistrationInformation();
    if (mRegistrationInformation == null) {
      throw new IllegalArgumentException("registrationInformation == null");
    }

    mUpdateSourceRegistration = mRegistrationInformation
      .isSourcesToBeUpdatedAtServiceCreation();
    mHandler = new Handler();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent intent, int startId) {
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      IntentRunner runner = new IntentRunner(intent, startId) {
        @Override
        public void run() {
          ExtensionService.this.mStartId = mRunnerStartId;

          switch (mIntent.getAction()) {
            case Registration.Intents.EXTENSION_REGISTER_REQUEST_INTENT:
              onRegisterRequest();
              break;

            case Intent.ACTION_LOCALE_CHANGED:
              onLocaleChanged();
              stopSelfCheck();
              break;

            case Registration.Intents.ACCESSORY_CONNECTION_INTENT:
              int status = mIntent.getIntExtra(Registration.Intents.EXTRA_CONNECTION_STATUS, -1);
              onConnectionChanged(status == Registration.AccessoryConnectionStatus.STATUS_CONNECTED);
              if (status == Registration.AccessoryConnectionStatus.STATUS_DISCONNECTED) {
                // Accessory disconnected.
                stopSelfCheck();
              } else {
                stopSelfCheck(true);
              }
              break;

            case Notification.Intents.VIEW_EVENT_INTENT:
            case Notification.Intents.REFRESH_REQUEST_INTENT:
              handleNotificationIntent(mIntent);
              stopSelfCheck(true);
              break;

            case Control.Intents.CONTROL_START_INTENT:
            case Control.Intents.CONTROL_STOP_INTENT:
            case Control.Intents.CONTROL_RESUME_INTENT:
            case Control.Intents.CONTROL_PAUSE_INTENT:
            case Control.Intents.CONTROL_ACTIVE_POWER_SAVE_MODE_STATUS_CHANGED_INTENT:
            case Control.Intents.CONTROL_ERROR_INTENT:
            case Control.Intents.CONTROL_KEY_EVENT_INTENT:
            case Control.Intents.CONTROL_TOUCH_EVENT_INTENT:
            case Control.Intents.CONTROL_OBJECT_CLICK_EVENT_INTENT:
            case Control.Intents.CONTROL_LIST_REQUEST_ITEM_INTENT:
            case Control.Intents.CONTROL_LIST_ITEM_CLICK_INTENT:
            case Control.Intents.CONTROL_LIST_REFRESH_REQUEST_INTENT:
            case Control.Intents.CONTROL_LIST_ITEM_SELECTED_INTENT:
            case Control.Intents.CONTROL_MENU_ITEM_SELECTED:
            case Control.Intents.CONTROL_SWIPE_EVENT_INTENT:
              handleControlIntent(mIntent);
              stopSelfCheck(true);
              break;
          }
        }
      };
      // post on handler to return quicker since started from broadcast
      // receiver
      mHandler.post(runner);
    }

    return START_STICKY;
  }

  /**
   * {@inheritDoc}
   *
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
    if (mRegisterTask != null) {
      mRegisterTask.setRegisterInterface(null);
      mRegisterTask.cancel(true);
      mRegisterTask = null;
    }

    destroyAllControls();

    super.onDestroy();
  }

  /**
   * Perform extension registration in background Override this method to do
   * anything else when locale change
   */
  protected void onLocaleChanged() {
    registerOrUpdate(false);
  }

  /**
   * Called when accessory is connected/disconnected Override this method to
   * handle connection/disconnection of accessory
   *
   * @param connected True on source registration refresh success.
   */
  protected void onConnectionChanged(boolean connected) {

  }

  /**
   * Perform extension registration in background Override this method to
   * handle registration
   */
  protected void onRegisterRequest() {
    registerOrUpdate(false);
  }

  /**
   * Perform extension registration in background.
   *
   * @param onlySources True if only sources shall be refreshed. False for
   *                    full registration update.
   */
  protected void registerOrUpdate(boolean onlySources) {
    mUpdateSourceRegistration = false;

    if (mRegisterTask != null) {
      if (Dbg.DEBUG) {
        Dbg.d("Registration already on-going. Queueing new request.");
      }
      // New registration request received when we are already busy
      // handling a registration request.
      // Run this registration when we have finished the first one.
      // This to handle the case when a host app installs when we
      // are busy registering ourselves.
      mPendingNewRegistration = true;
      return;
    }

    mRegisterTask = new RegisterExtensionTask(this, mRegistrationInformation, this, onlySources);
    mRegisterTask.execute();
    mPendingNewRegistration = false;
  }

  @Override
  public final void onExtensionRegisterResult(boolean onlySources, boolean success) {
    mRegisterTask = null;

    if (mPendingNewRegistration) {
      registerOrUpdate(false);
    } else {
      // Notify extension
      if (onlySources) {
        onSourceRefreshResult(success);
      } else {
        onRegisterResult(success);
      }

      // Check if the service shall be run or shall be stopped.
      stopSelfCheck();
    }
  }

  /**
   * Called after extension registration Override this method if you want to
   * take actions after registration.
   *
   * @param success True on register success, false otherwise
   */
  public void onRegisterResult(boolean success) {

  }

  /**
   * Called after source registration has been refreshed. Override this method
   * if you want to take actions after a source registration refresh.
   *
   * @param success True on source registration refresh success.
   */
  public void onSourceRefreshResult(boolean success) {

  }

  /**
   * Handle VIEW_EVENT_INTENT. Override this method if this is a notification
   * extension
   *
   * @param intent The view intent
   */
  protected void onViewEvent(Intent intent) {

  }

  /**
   * Handle REFRESH_REQUEST_INTENT. Sync extension data in this callback. This
   * is only relevant for extensions that aren't always up to date, like
   * polling extensions. Override this method if this is a notification
   * extension
   */
  protected void onRefreshRequest() {

  }

  /**
   * Get the extension registration information
   *
   * @return The extension registration information.
   */
  protected abstract RegistrationInformation getRegistrationInformation();

  /**
   * Shall the extension service be kept running as long as an accessory is
   * connected.
   *
   * @return True if the service shall be kept running as long an accessory is
   * connected.
   */
  protected abstract boolean keepRunningWhenConnected();

  /**
   * Stop the service if there is no activities that requires the service to
   * be running.
   *
   * @param accessoryConnected False if accessory is not connected.
   */
  private void stopSelfCheck(boolean accessoryConnected) {
    if (Dbg.DEBUG) {
      Dbg.d("stopSelfCheck: " + accessoryConnected);
    }

    if (mRegisterTask != null) {
      // Registration on-going do not stop.
      if (Dbg.DEBUG) {
        Dbg.d("registration on-going not stopping");
      }
      return;
    }

    if (mUpdateSourceRegistration) {
      // The source registration shall be refreshed.
      registerOrUpdate(true);
      return; // Not stopping because we just started a registration.
    }

    if (!accessoryConnected) {
      // No accessory connected. Stop service.
      // There is little point in doing anything if there
      // is no accessory connected.
      stopSelf(mStartId);
      return;
    }

    if (mControls.size() > 0) {
      // Control is visible. Do not stop service.
      if (Dbg.DEBUG) {
        Dbg.d("control is visible. Not stopping");
      }
      return;
    }

    if (!keepRunningWhenConnected()) {
      // If the extension does not require that the service is
      // running when there is an accessory connected we may stop it now.
      stopSelf(mStartId);
    } else {
      if (Dbg.DEBUG) {
        Dbg.d("keep running when connected. Not stopping");
      }
    }
  }

  /**
   * Stop the service if there is no activities that requires the service to
   * be running.
   */
  protected final void stopSelfCheck() {
    stopSelfCheck(areAnyAccessoriesConnected());
  }

  /**
   * Create control extension. Override this method to provide extension
   * control.
   *
   * @param hostAppPackageName The host application package name.
   * @return The control extension.
   * @see RegistrationInformation#getRequiredControlApiVersion()
   * @see ControlExtension
   */
  public ControlExtension createControlExtension(String hostAppPackageName) {
    throw new IllegalArgumentException(
      "createControlExtension() not implemented. Control extensions must override this method");
  }

  /**
   * Trigger action for all controls. Use this method to take the same action
   * on all controls.
   *
   * @param requestCode Code defined by the caller used to distinguish between
   *                    different actions.
   * @param bundle      Optional bundle with additional information.
   */
  public void doActionOnAllControls(int requestCode, Bundle bundle) {
    for (ControlExtension control : mControls.values())
      control.onDoAction(requestCode, bundle);
  }

  /**
   * Trigger action on a control.
   *
   * @param requestCode        Code defined by the caller used to distinguish between
   *                           different actions.
   * @param hostAppPackageName The host app package name.
   * @param bundle             Optional bundle with additional information.
   * @return True if the widget exists. False otherwise.
   */
  public boolean doActionOnControl(int requestCode, String hostAppPackageName, Bundle bundle) {
    ControlExtension control = mControls.get(hostAppPackageName);
    if (control != null) {
      control.onDoAction(requestCode, bundle);
      return true;
    }
    return false;
  }

  /**
   * Destroy all controls. Inform all controls that they shall free any
   * resources such as threads and registered broad cast receivers.
   */
  public void destroyAllControls() {
    for (ControlExtension control : mControls.values())
      control.destroy();
  }

  /**
   * Handle notification intent.
   *
   * @param intent The intent to handle.
   */
  private void handleNotificationIntent(final Intent intent) {
    String action = intent.getAction();

    if (!mExtensionKey.equals(intent.getStringExtra(Notification.Intents.EXTRA_EXTENSION_KEY))) {
      if (Dbg.DEBUG) {
        Dbg.w("Invalid extension key: "
              + intent.getStringExtra(Notification.Intents.EXTRA_EXTENSION_KEY));
      }
      return;
    }

    if (Notification.Intents.VIEW_EVENT_INTENT.equals(action)) {
      onViewEvent(intent);
    } else if (Notification.Intents.REFRESH_REQUEST_INTENT.equals(action)) {
      onRefreshRequest();
    }
  }

  /**
   * Handle control intent.
   *
   * @param intent The intent to handle.
   */
  private void handleControlIntent(final Intent intent) {

    if (!mExtensionKey.equals(intent.getStringExtra(Control.Intents.EXTRA_EXTENSION_KEY))) {
      if (Dbg.DEBUG) {
        Dbg.w("Invalid extension key: "
              + intent.getStringExtra(Control.Intents.EXTRA_EXTENSION_KEY));
      }
      return;
    }

    String action = intent.getAction();
    String hostAppPackageName = intent.getStringExtra(Control.Intents.EXTRA_AHA_PACKAGE_NAME);

    // Lookup control based on host application package name.
    ControlExtension control = mControls.get(hostAppPackageName);
    if (control == null) {
      if (Control.Intents.CONTROL_STOP_INTENT.equals(action)) {
        if (Dbg.DEBUG) {
          Dbg.w("No control object for: " + hostAppPackageName + ". Ignoring stop.");
        }
        return;
      } else if (Control.Intents.CONTROL_ERROR_INTENT.equals(action)) {
        onControlError(hostAppPackageName,
                       intent.getIntExtra(Control.Intents.EXTRA_ERROR_CODE, -1));
        return;
      }

      // Create new control and add it to the list of active controls.

      // We do this not only for start intents since the process might be
      // killed after the start intent, and in that case we need to
      // recreate the control object when we get a new intent.
      // Otherwise it will be experienced as the that the control is not
      // responding to user actions.
      control = createControlExtension(hostAppPackageName);
      mControls.put(hostAppPackageName, control);

      control.start();

      if (!Control.Intents.CONTROL_START_INTENT.equals(action)) {
        if (Dbg.DEBUG) {
          Dbg.w("No control object for: " + hostAppPackageName + ". Creating one.");
        }

        if (!Control.Intents.CONTROL_PAUSE_INTENT.equals(action)) {
          // If it wasn't a pause intent then assume that the control
          // is
          // foreground and also call resume.
          if (Dbg.DEBUG) {
            Dbg.w("Calling faked resume");
          }
          control.resume();
        }
      }
    } else {
      if (Control.Intents.CONTROL_START_INTENT.equals(action)) {
        // Ignoring start for already started.
        if (Dbg.DEBUG) {
          Dbg.w("Ignoring start for: " + hostAppPackageName + ". Already started.");
        }
      }
    }

    switch (action) {
      case Control.Intents.CONTROL_STOP_INTENT:
        control.stop();

        // Destroy the control and remove it from the list of active
        // controls.
        control.destroy();
        mControls.remove(hostAppPackageName);
        break;

      case Control.Intents.CONTROL_RESUME_INTENT:
        control.resume();
        break;

      case Control.Intents.CONTROL_PAUSE_INTENT:
        control.pause();
        break;

      case Control.Intents.CONTROL_ERROR_INTENT:
        control.onError(intent.getIntExtra(Control.Intents.EXTRA_ERROR_CODE, -1));
        break;

      case Control.Intents.CONTROL_KEY_EVENT_INTENT:
        control.onKey(intent.getIntExtra(Control.Intents.EXTRA_KEY_ACTION, -1),
                      intent.getIntExtra(Control.Intents.EXTRA_KEY_CODE, -1),
                      intent.getLongExtra(Control.Intents.EXTRA_TIMESTAMP, 0));
        break;

      case Control.Intents.CONTROL_TOUCH_EVENT_INTENT: {
        ControlTouchEvent event = new ControlTouchEvent(intent.getIntExtra(
          Control.Intents.EXTRA_TOUCH_ACTION, -1), intent.getLongExtra(
          Control.Intents.EXTRA_TIMESTAMP, 0), intent.getIntExtra(
          Control.Intents.EXTRA_X_POS, -1), intent.getIntExtra(
          Control.Intents.EXTRA_Y_POS, -1));

        control.onTouch(event);
        break;
      }

      case Control.Intents.CONTROL_SWIPE_EVENT_INTENT:
        control.onSwipe(intent.getIntExtra(Control.Intents.EXTRA_SWIPE_DIRECTION, -1));
        break;

      case Control.Intents.CONTROL_OBJECT_CLICK_EVENT_INTENT: {
        ControlObjectClickEvent event = new ControlObjectClickEvent(intent.getIntExtra(
          Control.Intents.EXTRA_CLICK_TYPE, -1), intent.getLongExtra(
          Control.Intents.EXTRA_TIMESTAMP, 0), intent.getIntExtra(
          Control.Intents.EXTRA_LAYOUT_REFERENCE, -1));

        control.onObjectClick(event);
        break;
      }

      case Control.Intents.CONTROL_LIST_REQUEST_ITEM_INTENT:
        control.onRequestListItem(
          intent.getIntExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE, -1),
          intent.getIntExtra(Control.Intents.EXTRA_LIST_ITEM_POSITION, -1));
        break;

      case Control.Intents.CONTROL_LIST_ITEM_CLICK_INTENT: {
        ControlListItem listItem = new ControlListItem();
        listItem.layoutReference = intent.getIntExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE,
                                                      -1);
        listItem.listItemId = intent.getIntExtra(Control.Intents.EXTRA_LIST_ITEM_ID, -1);
        listItem.listItemPosition = intent.getIntExtra(
          Control.Intents.EXTRA_LIST_ITEM_POSITION, -1);

        control.onListItemClick(listItem,
                                intent.getIntExtra(Control.Intents.EXTRA_CLICK_TYPE, -1),
                                intent.getIntExtra(Control.Intents.EXTRA_LIST_ITEM_LAYOUT_REFERENCE, -1));
        break;
      }

      case Control.Intents.CONTROL_LIST_ITEM_SELECTED_INTENT: {
        ControlListItem listItem = new ControlListItem();
        listItem.layoutReference = intent.getIntExtra(Control.Intents.EXTRA_LAYOUT_REFERENCE,
                                                      -1);
        listItem.listItemId = intent.getIntExtra(Control.Intents.EXTRA_LIST_ITEM_ID, -1);
        listItem.listItemPosition = intent.getIntExtra(
          Control.Intents.EXTRA_LIST_ITEM_POSITION, -1);

        control.onListItemSelected(listItem);
        break;
      }

      case Control.Intents.CONTROL_LIST_REFRESH_REQUEST_INTENT:
        Dbg.d("List refresh");
        break;

      case Control.Intents.CONTROL_MENU_ITEM_SELECTED:
        control.onMenuItemSelected(intent.getIntExtra(Control.Intents.EXTRA_MENU_ITEM_ID, -1));
        break;

      case Control.Intents.CONTROL_ACTIVE_POWER_SAVE_MODE_STATUS_CHANGED_INTENT:
        int powerState = intent.getIntExtra(Control.Intents.EXTRA_ACTIVE_POWER_MODE_STATUS,
                                            Control.Intents.ACTIVE_POWER_SAVE_MODE_OFF);
        control.onActiveLowPowerModeChange(powerState == Control.Intents.ACTIVE_POWER_SAVE_MODE_ON);
        break;
    }
  }

  /**
   * Check in the database if there are any accessories connected.
   *
   * @return True if at least one accessories is connected.
   */
  protected boolean areAnyAccessoriesConnected() {
    Cursor cursor = null;
    try {
      cursor = getContentResolver().query(Device.URI, null,
                                          DeviceColumns.ACCESSORY_CONNECTED + " = 1", null, null);
      if (cursor != null) {
        return (cursor.getCount() > 0);
      }
    } catch (SQLException | SecurityException | IllegalArgumentException exception) {
      if (Dbg.DEBUG) {
        Dbg.e("Failed to query connected accessories", exception);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return false;
  }

  /**
   * Send control start request.
   *
   * @param hostAppPackageName The host application package to start a control
   *                           for.
   */
  protected void controlStartRequest(String hostAppPackageName) {
    Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
    intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, getPackageName());
    intent.setPackage(hostAppPackageName);
    sendBroadcast(intent, Registration.HOSTAPP_PERMISSION);
  }

  /**
   * A control error occurred and there is no control started for the host
   * application.
   *
   * @param hostAppPackageName The host application package name.
   * @param errorCode          The error code. {@link Control.Intents#EXTRA_ERROR_CODE}
   */
  protected void onControlError(String hostAppPackageName, int errorCode) {

  }
}
