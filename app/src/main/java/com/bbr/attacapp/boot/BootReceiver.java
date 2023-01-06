/******************************************************************************
 *     Copyright (C) by ETHZ/SED                                              *
 *                                                                            *
 *   This program is free software: you can redistribute it and/or modify     *
 *   it under the terms of the GNU Affero General Public License as published *
 *   by the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                      *
 *                                                                            *
 *   This program is distributed in the hope that it will be useful,          *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            *
 *   GNU Affero General Public License for more details.                      *
 *                                                                            *
 *   -----------------------------------------------------------------------  *
 *                                                                            *
 *   @author: Billy Burgoa Rosso                                               *
 *   Independent Consultant       <billyburgoa@gmail.com>                     *
 *                                                                            *
 ******************************************************************************/
/**
* This class is to start the foreground receiver
* after the OS has been started or restarted.
*
* Having a foreground service might help the app
* to be active for the OS and avoiding to be put into
* the restricted apps. This cannot be enough for android 13
* and later versions. See the documentation about this topic.
*
* */
package com.bbr.attacapp.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bbr.attacapp.services.BackgroundLocationService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent locationService = new Intent(context, BackgroundLocationService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(locationService);
            }

        }
    }
}