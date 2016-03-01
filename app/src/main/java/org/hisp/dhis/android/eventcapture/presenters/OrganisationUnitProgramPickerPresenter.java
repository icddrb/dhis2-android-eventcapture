package org.hisp.dhis.android.eventcapture.presenters;

import org.hisp.dhis.android.eventcapture.mapper.OrganisationUnitPickableMapper;
import org.hisp.dhis.android.eventcapture.mapper.ProgramPickableMapper;
import org.hisp.dhis.android.eventcapture.utils.AbsPresenter;
import org.hisp.dhis.android.eventcapture.views.IOrganisationUnitProgramPickerView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.ui.views.chainablepickerview.IPickable;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class OrganisationUnitProgramPickerPresenter extends AbsPresenter {

    private IOrganisationUnitProgramPickerView mOrganisationUnitProgramPickerView;
    private OrganisationUnitPickableMapper mOrganisationUnitPickableMapper;
    private ProgramPickableMapper mProgramPickableMapper;
    private Subscription programSubscription;
    private Subscription organisationUnitSubscription;
    private Subscription pickedOrganisationUnitSubscription;


    public OrganisationUnitProgramPickerPresenter() {
        mOrganisationUnitPickableMapper = new OrganisationUnitPickableMapper();
        mProgramPickableMapper = new ProgramPickableMapper();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.loadOrganisationUnits();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(organisationUnitSubscription != null && !organisationUnitSubscription.isUnsubscribed()) {
            organisationUnitSubscription.unsubscribe();
            organisationUnitSubscription = null;
        }

        if(programSubscription != null && !programSubscription.isUnsubscribed()) {
            programSubscription.unsubscribe();
            programSubscription = null;
        }

        if(pickedOrganisationUnitSubscription != null && !pickedOrganisationUnitSubscription.isUnsubscribed()) {
            pickedOrganisationUnitSubscription.unsubscribe();
            pickedOrganisationUnitSubscription = null;
        }
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    public void loadOrganisationUnits() {
        if(organisationUnitSubscription == null || organisationUnitSubscription.isUnsubscribed()) {
            mOrganisationUnitProgramPickerView.onStartLoading();

            organisationUnitSubscription = D2.organisationUnits().list()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<OrganisationUnit>>() {
                @Override
                public void call(List<OrganisationUnit> organisationUnits) {
                    setOrganisationUnitPickables(organisationUnits);
                    mOrganisationUnitProgramPickerView.onFinishLoading();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    mOrganisationUnitProgramPickerView.onLoadingError(); // (throwable);
                }
            });
        }
    }

    public void loadPrograms(OrganisationUnit organisationUnit) {
        if(programSubscription == null || programSubscription.isUnsubscribed()) {
            mOrganisationUnitProgramPickerView.onStartLoading();
            programSubscription = D2.programs().list(organisationUnit, ProgramType.WITHOUT_REGISTRATION)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Program>>() {
                @Override
                public void call(List<Program> programs) {
                    setProgramPickables(programs);
                    mOrganisationUnitProgramPickerView.onFinishLoading();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    mOrganisationUnitProgramPickerView.onLoadingError(); //(throwable);
                }
            });
        }
    }

    public void setOrganisationUnitPickables(List<OrganisationUnit> organisationUnits) {
        List<IPickable> organisationUnitPickables = mOrganisationUnitPickableMapper.transform(organisationUnits);
        mOrganisationUnitProgramPickerView.renderOrganisationUnitPickables(organisationUnitPickables);

    }

    public void setProgramPickables(List<Program> programs) {
        List<IPickable> programPickables = mProgramPickableMapper.transform(programs);
        mOrganisationUnitProgramPickerView.renderProgramPickables(programPickables);

    }

    public void setOrganisationUnitProgramPickerView(IOrganisationUnitProgramPickerView mOrganisationUnitProgramPickerView) {
        this.mOrganisationUnitProgramPickerView = mOrganisationUnitProgramPickerView;
    }

    public void setPickedOrganisationUnit(Observable<OrganisationUnit> organisationUnit) {
        if(pickedOrganisationUnitSubscription == null || pickedOrganisationUnitSubscription.isUnsubscribed()) {
            pickedOrganisationUnitSubscription = organisationUnit.
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(new Action1<OrganisationUnit>() {
                    @Override
                    public void call(OrganisationUnit organisationUnit) {
                        loadPrograms(organisationUnit);
                    }
                });

        }
    }
}