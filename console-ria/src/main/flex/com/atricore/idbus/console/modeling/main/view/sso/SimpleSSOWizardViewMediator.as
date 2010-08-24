/*
 * Atricore Console
 *
 * Copyright 2009-2010, Atricore Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.atricore.idbus.console.modeling.main.view.sso
{
import com.atricore.idbus.console.components.wizard.WizardEvent;
import com.atricore.idbus.console.main.ApplicationFacade;
import com.atricore.idbus.console.main.view.progress.ProcessingMediator;
import com.atricore.idbus.console.main.view.upload.UploadProgressMediator;
import com.atricore.idbus.console.modeling.main.controller.CreateSimpleSSOIdentityApplianceCommand;
import com.atricore.idbus.console.services.dto.IdentityAppliance;
import com.atricore.idbus.console.services.dto.IdentityApplianceDefinition;
import com.atricore.idbus.console.services.dto.IdentityVault;
import com.atricore.idbus.console.services.dto.Keystore;
import com.atricore.idbus.console.services.dto.ServiceProvider;

import flash.events.DataEvent;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.events.ProgressEvent;
import flash.net.FileFilter;
import flash.net.FileReference;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.events.CloseEvent;
import mx.utils.ObjectProxy;

import org.puremvc.as3.interfaces.INotification;
import org.springextensions.actionscript.puremvc.patterns.mediator.IocMediator;

public class SimpleSSOWizardViewMediator extends IocMediator
{
    public static const RUN:String = "Note.start.RunSimpleSSOSetup";

    private var _wizardDataModel:ObjectProxy = new ObjectProxy();

    [Bindable]
    private var _fileRef:FileReference;

    [Bindable]
    public var _resourceId:String;

    [Bindable]
    public var _selectedFiles:Array;

    private var _processingStarted:Boolean;

    public function SimpleSSOWizardViewMediator(name:String = null, viewComp:SimpleSSOWizardView = null) {
        super(name, viewComp);
    }

    override public function setViewComponent(viewComponent:Object):void {
        if (getViewComponent() != null) {
            view.addEventListener(WizardEvent.WIZARD_COMPLETE, onSimpleSSOWizardComplete);
            view.addEventListener(WizardEvent.WIZARD_CANCEL, onSimpleSSOWizardCancelled);
            view.addEventListener(CloseEvent.CLOSE, handleClose);

            if (_fileRef != null) {
                _fileRef.removeEventListener(Event.SELECT, fileSelectHandler);
                _fileRef.removeEventListener(ProgressEvent.PROGRESS, uploadProgressHandler);
                _fileRef.removeEventListener(Event.COMPLETE, uploadCompleteHandler);
                _fileRef.removeEventListener(DataEvent.UPLOAD_COMPLETE_DATA, uploadCompleteDataHandler);
            }
        }

        super.setViewComponent(viewComponent);

        init();
    }


    private function init():void {

        view.dataModel = _wizardDataModel;
        view.addEventListener(WizardEvent.WIZARD_COMPLETE, onSimpleSSOWizardComplete);
        view.addEventListener(WizardEvent.WIZARD_CANCEL, onSimpleSSOWizardCancelled);
        view.addEventListener(CloseEvent.CLOSE, handleClose);

        // upload bindings
        view.steps[1].btnUpload.addEventListener(MouseEvent.CLICK, handleUpload);
        view.steps[1].certificateKeyPair.addEventListener(MouseEvent.CLICK, browseHandler);

        _fileRef = new FileReference();
        _fileRef.addEventListener(Event.SELECT, fileSelectHandler);
        _fileRef.addEventListener(ProgressEvent.PROGRESS, uploadProgressHandler);
        _fileRef.addEventListener(Event.COMPLETE, uploadCompleteHandler);
        _fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, uploadCompleteDataHandler);

        BindingUtils.bindProperty(view.steps[1], "resourceId", this, "_resourceId");
        BindingUtils.bindProperty(view.steps[1].certificateKeyPair, "dataProvider", this, "_selectedFiles");
    }

    override public function listNotificationInterests():Array {
        return [CreateSimpleSSOIdentityApplianceCommand.FAILURE,
            CreateSimpleSSOIdentityApplianceCommand.SUCCESS,
            UploadProgressMediator.CREATED,
            UploadProgressMediator.UPLOAD_CANCELED];
    }

    override public function handleNotification(notification:INotification):void {
        switch (notification.getName()) {
            case CreateSimpleSSOIdentityApplianceCommand.SUCCESS :
                handleSSOSetupSuccess();
                break;
            case CreateSimpleSSOIdentityApplianceCommand.FAILURE :
                handleSSOSetupFailure();
                break;
            case UploadProgressMediator.CREATED:
                // upload progress window created, start upload
                if (_fileRef != null) {
                    sendNotification(ApplicationFacade.UPLOAD, _fileRef);
                } else {
                    view.steps[1].lblUploadMsg.text = "Upload error";
                    view.steps[1].lblUploadMsg.visible = true;
                }
                break;
            case UploadProgressMediator.UPLOAD_CANCELED:
                if (_fileRef != null)
                    _fileRef.cancel();
                break;
        }
    }

    private function onSimpleSSOWizardComplete(event:WizardEvent):void {
        /*
         var identityAppliance:IdentityAppliance = _wizardDataModel.applianceData;
         var identityApplianceDefinition:IdentityApplianceDefinition = identityAppliance.idApplianceDefinition;
         identityApplianceDefinition.identityVaults = new ArrayCollection();
         identityApplianceDefinition.identityVaults.addItem(createIdentityVault());

         identityApplianceDefinition.providers = new ArrayCollection();
         for (var i:int = 0; i < _wizardDataModel.step3Data.length; i++) {
         var sp:ServiceProvider = _wizardDataModel.step3Data[i] as ServiceProvider;
         identityApplianceDefinition.providers.addItem(sp);
         }
         */

        _processingStarted = true;
        view.dispatchEvent(new CloseEvent(CloseEvent.CLOSE));

        sendNotification(ProcessingMediator.START);

        var identityAppliance:IdentityAppliance = _wizardDataModel.applianceData;
        var identityApplianceDefinition:IdentityApplianceDefinition = identityAppliance.idApplianceDefinition;
        identityApplianceDefinition.identityVaults = new ArrayCollection();
        identityApplianceDefinition.identityVaults.addItem(createIdentityVault());

        identityApplianceDefinition.providers = new ArrayCollection();
        for (var i:int = 0; i < _wizardDataModel.step3Data.length; i++) {
            var sp:ServiceProvider = _wizardDataModel.step3Data[i] as ServiceProvider;
            identityApplianceDefinition.providers.addItem(sp);
        }

        var keystore:Keystore = _wizardDataModel.certificateData;
        identityApplianceDefinition.certificate = keystore;

        sendNotification(ApplicationFacade.CREATE_SIMPLE_SSO_IDENTITY_APPLIANCE, identityAppliance);
    }

    private function onSimpleSSOWizardCancelled(event:WizardEvent):void {
        //closeWizard();
    }

    private function createIdentityVault():IdentityVault {
        if ((_wizardDataModel.step1Data as IdentityVault).embedded) {
            return _wizardDataModel.step2EmbeddedData as IdentityVault;
        } else {
            return _wizardDataModel.step2ExternalData as IdentityVault;
        }
    }

    public function handleSSOSetupSuccess():void {
        sendNotification(ProcessingMediator.STOP);
        sendNotification(ApplicationFacade.DISPLAY_APPLIANCE_MODELER);
        sendNotification(ApplicationFacade.UPDATE_IDENTITY_APPLIANCE);
        sendNotification(ApplicationFacade.DIAGRAM_ELEMENT_CREATION_COMPLETE);
        sendNotification(ApplicationFacade.IDENTITY_APPLIANCE_LIST_LOAD);
        sendNotification(ApplicationFacade.SHOW_SUCCESS_MSG,
                "The SSO appliance has been successfully created.");
    }

    public function handleSSOSetupFailure():void {
        sendNotification(ProcessingMediator.STOP);
        sendNotification(ApplicationFacade.SHOW_ERROR_MSG,
                "There was an error creating simple SSO appliance.");
    }

    // upload functions
    private function browseHandler(event:MouseEvent):void {
        if (_fileRef == null) {
            _fileRef = new FileReference();
            _fileRef.addEventListener(Event.SELECT, fileSelectHandler);
            _fileRef.addEventListener(ProgressEvent.PROGRESS, uploadProgressHandler);
            _fileRef.addEventListener(Event.COMPLETE, uploadCompleteHandler);
            _fileRef.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, uploadCompleteDataHandler);
        }
        var fileFilter:FileFilter = new FileFilter("JKS(*.jks)", "*.jks");
        var fileTypes:Array = new Array(fileFilter);
        _fileRef.browse(fileTypes);
    }

    private function handleUpload(event:MouseEvent):void {
        //_fileRef.load();  //this is available from flash player 10 and maybe flex sdk 3.4
        //_fileRef.data;
        sendNotification(ApplicationFacade.SHOW_UPLOAD_PROGRESS, _fileRef);
    }

    private function fileSelectHandler(evt:Event):void {
        _selectedFiles = new Array();
        _selectedFiles.push(_fileRef.name);
        view.steps[1].certificateKeyPair.selectedIndex = 0;
        view.steps[1].btnUpload.enabled = true;
    }

    private function uploadProgressHandler(event:ProgressEvent):void {
        var numPerc:Number = Math.round((Number(event.bytesLoaded) / Number(event.bytesTotal)) * 100);
        sendNotification(UploadProgressMediator.UPDATE_PROGRESS, numPerc);
    }

    private function uploadCompleteHandler(event:Event):void {
        sendNotification(UploadProgressMediator.UPLOAD_COMPLETED);
        _fileRef = null;
        _selectedFiles = new Array();
        view.steps[1].btnUpload.enabled = false;
    }

    private function uploadCompleteDataHandler(event:DataEvent):void {
        //var xmlResponse:XML = XML(event.data);
        //resourceId = xmlResponse.elements("resource").attribute("id").toString();
        _resourceId = event.data;
        view.steps[1].lblUploadMsg.text = "Keystore successfully uploaded";
        view.steps[1].lblUploadMsg.visible = true;
    }

    private function handleClose(event:Event):void {
    }

    protected function get view():SimpleSSOWizardView
    {
        return viewComponent as SimpleSSOWizardView;
    }
}
}