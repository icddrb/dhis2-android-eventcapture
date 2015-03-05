/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.eventcapture;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.eventcapture.fragments.RegisterEventFragment;
import org.hisp.dhis2.android.eventcapture.fragments.SelectProgramFragment;
import org.hisp.dhis2.android.sdk.activities.LoginActivity;
import org.hisp.dhis2.android.sdk.activities.SplashActivity;
import org.hisp.dhis2.android.sdk.controllers.Dhis2;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.fragments.EditItemFragment;
import org.hisp.dhis2.android.sdk.fragments.FailedItemsFragment;
import org.hisp.dhis2.android.sdk.fragments.SettingsFragment;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis2.android.sdk.persistence.models.Program;


public class MainActivity extends Activity {

    public final static String CLASS_TAG = "MainActivity";

    private CharSequence title;

    private Fragment currentFragment = null;
    private SelectProgramFragment selectProgramFragment;
    private RegisterEventFragment registerEventFragment;
    private FailedItemsFragment failedItemsFragment;
    private EditItemFragment editItemFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dhis2Application.bus.register(this);
        setContentView(R.layout.activity_main);
        if(Dhis2.hasLoadedInitialData(this))
            showSelectProgramFragment();
        else
            Dhis2.loadInitialData(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsFragment();
        } else if(id == R.id.failed_items) {
            showFailedItemsFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle( CharSequence title )
    {
        this.title = title;
        runOnUiThread(new Runnable() {
            public void run() {
                //getSupportActionBar().setTitle( MainActivity.this.title );
            }
        });
    }

    @Subscribe
    public void onReceiveMessage(MessageEvent event) {
        Log.d(CLASS_TAG, "onreceivemessage");
        if(event.eventType == BaseEvent.EventType.showRegisterEventFragment) {
            showRegisterEventFragment();
        } else if(event.eventType == BaseEvent.EventType.showSelectProgramFragment) {
            showSelectProgramFragment();
        } else if(event.eventType == BaseEvent.EventType.showEditItemFragment) {
            showEditItemFragment();
        } else if(event.eventType == BaseEvent.EventType.showFailedItemsFragment ) {
            showFailedItemsFragment();
        } else if(event.eventType == BaseEvent.EventType.logout) {
            logout();
        } else if(event.eventType == BaseEvent.EventType.onLoadingInitialDataFinished) {
            if(Dhis2.hasLoadedInitialData(this)) {
                showSelectProgramFragment();
            } else {
                //todo: notify the user that data is missing and request to try to re-load.
            }
        }
    }

    public void logout() {
        Dhis2.logout(this);
        showLoginActivity();
    }

    public void showLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void showFailedItemsFragment() {
        setTitle("Failed Items");
        if(failedItemsFragment == null) failedItemsFragment = new FailedItemsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, failedItemsFragment);
        fragmentTransaction.commit();
        currentFragment = failedItemsFragment;
    }

    public void showRegisterEventFragment() {
        setTitle("Register Event");
        registerEventFragment = new RegisterEventFragment();
        OrganisationUnit organisationUnit = selectProgramFragment.getSelectedOrganisationUnit();
        Program program = selectProgramFragment.getSelectedProgram();
        registerEventFragment.setSelectedOrganisationUnit(organisationUnit);
        registerEventFragment.setSelectedProgram(program);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, registerEventFragment);
        fragmentTransaction.commit();
        currentFragment = registerEventFragment;
    }

    public void showSelectProgramFragment() {
        setTitle("Event Capture");
        if(selectProgramFragment == null) selectProgramFragment = new SelectProgramFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, selectProgramFragment);
        fragmentTransaction.commit();
        currentFragment = selectProgramFragment;
    }

    public void showEditItemFragment() {
        setTitle("Edit Item");
        editItemFragment = new EditItemFragment();
        if(failedItemsFragment == null) return;
        editItemFragment.setItem(failedItemsFragment.getSelectedFailedItem());
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, editItemFragment);
        fragmentTransaction.commit();
        currentFragment = editItemFragment;
    }

    public void showSettingsFragment() {
        setTitle("Settings");
        if( settingsFragment == null ) settingsFragment = new SettingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
        fragmentTransaction.commit();
        currentFragment = settingsFragment;
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if ( (keyCode == KeyEvent.KEYCODE_BACK) )
        {
            if ( currentFragment == selectProgramFragment )
            {
                Dhis2.getInstance().showConfirmDialog(this, getString(R.string.confirm),
                        getString(R.string.exit_confirmation), getString(R.string.yes_option),
                        getString(R.string.no_option),
                 new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        finish();
                        System.exit( 0 );
                    }
                } );
            }
            else if ( currentFragment == registerEventFragment)
            {
                Dhis2.getInstance().showConfirmDialog(this, getString(R.string.discard),
                        getString(R.string.discard_confirm), getString(R.string.yes_option),
                        getString(R.string.no_option),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialog, int which )
                            {
                                showSelectProgramFragment();
                                registerEventFragment = null;
                            }
                        } );
            }
            else if ( currentFragment == failedItemsFragment ) {
                showSelectProgramFragment();
            }
            return true;
        }

        return super.onKeyDown( keyCode, event );
    }
}
