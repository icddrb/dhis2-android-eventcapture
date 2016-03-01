package org.hisp.dhis.android.eventcapture.fragments.itemlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import org.hisp.dhis.android.eventcapture.EventCaptureApp;
import org.hisp.dhis.android.eventcapture.fragments.dataentry.DataEntryActivity;
import org.hisp.dhis.android.eventcapture.fragments.selector.SelectorFragment;
import org.hisp.dhis.android.eventcapture.presenters.ItemListPresenter;
import org.hisp.dhis.android.eventcapture.utils.ActivityUtils;
import org.hisp.dhis.android.eventcapture.utils.RxBus;
import org.hisp.dhis.android.eventcapture.views.IItemListView;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRow;
import org.hisp.dhis.client.sdk.ui.views.itemlistrowview.ItemListRowAdapter;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class ItemListFragment extends org.hisp.dhis.client.sdk.ui.fragments.ItemListFragment implements IItemListView {
    public static final String TAG = ItemListFragment.class.getSimpleName();
    public static final String ORG_UNIT_ID = "extra:orgUnitId";
    public static final String PROGRAM_ID = "extra:ProgramId";

    private ItemListRowAdapter mItemListRowAdapter;
    private ItemListPresenter mItemListPresenter;
    private Subscription busSubscription;
    private RxBus rxBus;
    private Observable<OrganisationUnit> organisationUnitObservable;
    private Observable<Program> programObservable;

    public ItemListFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemListPresenter = new ItemListPresenter();
        mItemListPresenter.setItemListView(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rxBus = ((EventCaptureApp) getActivity().getApplication()).getRxBusSingleton();
    }

    @Override
    public void onStart() {
        super.onStart();

        busSubscription = rxBus.toObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof SelectorFragment.OnOrganisationUnitPickerValueUpdated) {
                    SelectorFragment.OnOrganisationUnitPickerValueUpdated onOrgUnitSelectedClick =
                            (SelectorFragment.OnOrganisationUnitPickerValueUpdated) event;
                    setOrganisationUnitObservable(onOrgUnitSelectedClick.getOrganisationUnitObservable());
                }
                if (event instanceof SelectorFragment.OnProgramPickerValueUpdated) {
                    SelectorFragment.OnProgramPickerValueUpdated onProgramSelectedClick =
                            (SelectorFragment.OnProgramPickerValueUpdated) event;
                    setProgramObservable(onProgramSelectedClick.getProgramObservable());
                }

                if(organisationUnitObservable != null && programObservable != null) {
                    mItemListPresenter.loadEventList
                            (organisationUnitObservable.toBlocking().first(),
                                    programObservable.toBlocking().first());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (busSubscription != null && !busSubscription.isUnsubscribed()) {
            busSubscription.unsubscribe();
            busSubscription= null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void renderItemRowList(List<ItemListRow> itemListRowCollection) {
        if(itemListRowCollection!= null) {
            mItemListRowAdapter = new ItemListRowAdapter(itemListRowCollection);
            mRecyclerView.setAdapter(mItemListRowAdapter);

            for(ItemListRow itemListRow : itemListRowCollection) {
                itemListRow.setOnRowClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), DataEntryActivity.class);
                        intent.putExtra(PROGRAM_ID,
                                programObservable.toBlocking().first().getUId());
                        intent.putExtra(ORG_UNIT_ID,
                                organisationUnitObservable.toBlocking().first().getUId());

                        getContext().startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public void viewEditEvent(Object object) {
        if(object instanceof Event) {
            Event event = (Event) object;
            //show dataEntryFragment
        }
    }

    @Override
    public void hideViewRetry() {

    }

    @Override
    public void showViewRetry() {

    }

    @Override
    public void hideViewLoading() {

    }

    @Override
    public void showViewLoading() {

    }

    @Override
    public void showErrorMessage(String error) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mItemListPresenter.onCreate();
    }

    public void setOrganisationUnitObservable(Observable<OrganisationUnit> organisationUnitObservable) {
        this.organisationUnitObservable = organisationUnitObservable;
    }

    public void setProgramObservable(Observable<Program> programObservable) {
        this.programObservable = programObservable;
    }
}