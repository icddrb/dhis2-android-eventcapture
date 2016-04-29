/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.presenters;


import android.support.v4.util.Pair;

import org.hisp.dhis.android.eventcapture.views.fragments.EventDataEntryView;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.client.sdk.ui.models.DataEntity;
import org.hisp.dhis.client.sdk.ui.models.DataEntityText;
import org.hisp.dhis.client.sdk.ui.models.OnValueChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventDataEntryPresenterImpl implements EventDataEntryPresenter {
    private EventDataEntryView eventDataEntryView;
    private Subscription programDataEntryRowSubscription;
    private Subscription saveDataEntityValues;

    public EventDataEntryPresenterImpl(EventDataEntryView eventDataEntryView) {
        this.eventDataEntryView = eventDataEntryView;
    }

//    public void onDestroy() {
//        if (programDataEntryRowSubscription != null && !programDataEntryRowSubscription.isUnsubscribed()) {
//            programDataEntryRowSubscription.unsubscribe();
//        }
//
//        if(saveDataEntityValues != null && !saveDataEntityValues.isUnsubscribed()) {
//            saveDataEntityValues.unsubscribe();
//        }
//
//        programDataEntryRowSubscription = null;
//        saveDataEntityValues = null;
//        eventDataEntryView = null;
//    }

    @Override
    public void listDataEntryFields(String programStageSectionUid) {
        programDataEntryRowSubscription = D2.programStageSections().get(programStageSectionUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ProgramStageSection, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(ProgramStageSection programStageSection) {
                        return D2.programStageDataElements().list(programStageSection).toBlocking().first();
                    }
                })
                .map(new Func1<List<ProgramStageDataElement>, List<DataEntity>>() {
                    @Override
                    public List<DataEntity> call(List<ProgramStageDataElement> programStageDataElements) {
                        return transformDataEntryForm(programStageDataElements);
                    }
                })
                .subscribe(new Action1<List<DataEntity>>() {
                    @Override
                    public void call(List<DataEntity> dataEntities) {
                        if (eventDataEntryView != null) {
                            eventDataEntryView.setDataEntryFields(dataEntities);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void listDataEntryFieldsWithEventValues(final String eventUId, final String programStageSectionUId) {

//        Observable<List<ProgramStageDataElement>> programStageDataElements = D2.programStageSections().get(programStageSectionUId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Func1<ProgramStageSection, List<ProgramStageDataElement>>() {
//                    @Override
//                    public List<ProgramStageDataElement> call(ProgramStageSection programStageSection) {
//                        return D2.programStageDataElements().list(programStageSection).toBlocking().first();
//                    }
//                });
//
//        Observable<HashMap<String, TrackedEntityDataValue>> programStageDataElementHash = programStageDataElements.zipWith(D2.events().get(eventUId),
//                new Func2<List<ProgramStageDataElement>, Event, HashMap<String, TrackedEntityDataValue>>() {
//                    @Override
//                    public HashMap<String, TrackedEntityDataValue> call(List<ProgramStageDataElement> programStageDataElements, Event event) {
//                        HashMap<String, TrackedEntityDataValue> map = new HashMap<>();
//                        List<TrackedEntityDataValue> trackedEntityDataValues = event.getTrackedEntityDataValues();
//                        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
//                            map.put(programStageDataElement.getUId(), null);
//                        }
//                        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
//                            if (map.containsKey(trackedEntityDataValue.getDataElement())) {
//                                map.put(trackedEntityDataValue.getDataElement(), trackedEntityDataValue);
//                            }
//                        }
//
//                        return map;
//                    }
//                });
//        Observable programStageSections = D2.programStageSections().get(programStageSectionUId);
//
//        Observable<List<IDataEntity>> dataEntryForm = Observable.combineLatest(programStageDataElementHash, programStageSections, new Func2<ProgramStageSection,
//                HashMap<String, TrackedEntityDataValue>, List<IDataEntity>>() {
//            @Override
//            public List<IDataEntity> call(ProgramStageSection section, HashMap<String, TrackedEntityDataValue> valueHashMap) {
//                return transformDataEntryFormWithValues(valueHashMap, section, D2.events().get(eventUId).toBlocking().first());
//            }
//        });
//
//
//        programDataEntryRowSubscription = dataEntryForm.subscribe(new Action1<List<IDataEntity>>() {
//            @Override
//            public void call(List<IDataEntity> dataEntities) {
//                if (eventDataEntryView != null) {
//                    eventDataEntryView.setDataEntryFields(dataEntities);
//                }
//            }
//        }, new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//                Timber.d(throwable.toString());
//            }
//        });
    }

    private List<DataEntity> transformDataEntryForm(List<ProgramStageDataElement> programStageDataElements) {
        List<DataEntity> dataEntities = new ArrayList<>();

        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();
        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);
//            TrackedEntityDataValue trackedEntityDataValue = valueHashMap.get(programStageDataElement.getUId());
//            dataEntityValueChangedListener.setTrackedEntityDataValue(trackedEntityDataValue);

            if (programStageDataElement != null && programStageDataElement.getDataElement() != null) {

                if (programStageDataElement.getDataElement().getOptionSet() != null) {
                    dataEntities.add(null);
                }
                if (programStageDataElement.getDataElement().getValueType().isBoolean()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.BOOLEAN, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.COORDINATES, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isDate()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.DATE, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isFile()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.FILE, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isInteger()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.INTEGER, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isNumeric()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.NUMBER, dataEntityValueChangedListener));
                } else if (programStageDataElement.getDataElement().getValueType().isText()) {
                    dataEntities.add(DataEntityText.create(
                            programStageDataElement.getDataElement().getDisplayName(),
                            "",//trackedEntityDataValue.getValue(),
                            DataEntityText.Type.TEXT, dataEntityValueChangedListener));
                }
            }
        }

        return dataEntities;
    }

    private List<DataEntity> transformDataEntryFormWithValues(
            HashMap<String, TrackedEntityDataValue> valueHashMap, ProgramStageSection section, Event event) {

        List<DataEntity> dataEntities = new ArrayList<>();
        List<ProgramStageDataElement> programStageDataElements = section.getProgramStageDataElements();
        RxDataEntityValueChangedListener dataEntityValueChangedListener = new RxDataEntityValueChangedListener();
        dataEntityValueChangedListener.setEvent(event);

        if (section.getProgramStage().getReportDateDescription() != null) {
            dataEntities.add(DataEntityText.create(
                    section.getProgramStage().getReportDateDescription(),
                    event.getEventDate().toString(), DataEntityText.Type.DATE, dataEntityValueChangedListener));
        }
        if (section.getProgramStage().isCaptureCoordinates()) {
            //coordinate row
            // TODO create onvaluechangedlistener with getting coordinates


            // dataEntities.add(DataEntityCoordinate.create("Capture Coordinates", event.getCoordinate(), DataEntityText.Type.COORDINATES));
        }

        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
            dataEntityValueChangedListener.setProgramStageDataElement(programStageDataElement);
            TrackedEntityDataValue trackedEntityDataValue = valueHashMap.get(programStageDataElement.getUId());
            dataEntityValueChangedListener.setTrackedEntityDataValue(trackedEntityDataValue);

            if (programStageDataElement.getDataElement().getValueType().isBoolean()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.BOOLEAN, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isCoordinate()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.COORDINATES, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isDate()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.DATE, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isFile()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.FILE, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isInteger()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.INTEGER, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isNumeric()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.NUMBER, dataEntityValueChangedListener));
            } else if (programStageDataElement.getDataElement().getValueType().isText()) {
                dataEntities.add(DataEntityText.create(
                        programStageDataElement.getDataElement().getDisplayName(),
                        trackedEntityDataValue.getValue(),
                        DataEntityText.Type.TEXT, dataEntityValueChangedListener));
            }
        }

        return dataEntities;
    }

    private class RxDataEntityValueChangedListener implements OnValueChangeListener<Pair<CharSequence, CharSequence>> {
        private ProgramStageDataElement programStageDataElement;
        private TrackedEntityDataValue trackedEntityDataValue;
        private Event event;
        private Observable saveValueObservable;


        @Override
        public void onValueChanged(Pair<CharSequence, CharSequence> keyValuePair) {

//            if(programStageDataElement.getProgramStage().getReportDateDescription().equals(keyValuePair.first)) {
//                // update report date in Event and save
//                event.setEventDate(new DateTime(keyValuePair.second.toString()));
//                saveValueObservable = D2.events().save(event);
//            }
//            else if("Capture Coordinates".equals(keyValuePair.first)) {
//                // update coordinates in Event and save
//                // needs special onvaluechangedlistener
//
//            }
//            else if(programStageDataElement.getDataElement().getDisplayName().equals(keyValuePair.first)) {
//                // save trackedEntityDataValue
//                trackedEntityDataValue.setValue(keyValuePair.second.toString());
//                saveValueObservable = D2.trackedEntityDataValues().save(trackedEntityDataValue);
//            }
//
//
//            saveDataEntityValues = D2.trackedEntityDataValues().save(trackedEntityDataValue)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .debounce(250, TimeUnit.MILLISECONDS)
//                    .map(new Func1<Boolean, List<ProgramRule>>() {
//                        @Override
//                        public List<ProgramRule> call(Boolean aBoolean) {
//                            return D2.programRules().list(programStageDataElement.getProgramStage()).toBlocking().first();
//                        }
//                    }).subscribe(new Action1<List<ProgramRule>>() {
//                        @Override
//                        public void call(List<ProgramRule> programRules) {
//
//                        }
//                    });

            // trigger update of program rules
        }

        public void setProgramStageDataElement(ProgramStageDataElement programStageDataElement) {
            this.programStageDataElement = programStageDataElement;
        }

        public void setTrackedEntityDataValue(TrackedEntityDataValue trackedEntityDataValue) {
            this.trackedEntityDataValue = trackedEntityDataValue;
        }

        public void setEvent(Event event) {
            this.event = event;
        }
    }
}