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

package com.atricore.idbus.console.config.main.controller
{
import com.atricore.idbus.console.config.main.model.ServiceConfigProxy;
import com.atricore.idbus.console.main.ApplicationFacade;
import com.atricore.idbus.console.main.service.ServiceRegistry;
import com.atricore.idbus.console.services.dto.settings.ArtifactQueueManagerConfiguration;
import com.atricore.idbus.console.services.dto.settings.HttpServiceConfiguration;
import com.atricore.idbus.console.services.dto.settings.LogServiceConfiguration;
import com.atricore.idbus.console.services.dto.settings.ManagementServiceConfiguration;
import com.atricore.idbus.console.services.dto.settings.PersistenceServiceConfiguration;
import com.atricore.idbus.console.services.dto.settings.ServiceConfiguration;
import com.atricore.idbus.console.services.dto.settings.ServiceType;
import com.atricore.idbus.console.services.dto.settings.SshServiceConfiguration;

import mx.messaging.messages.ErrorMessage;
import mx.rpc.IResponder;
import mx.rpc.events.FaultEvent;
import mx.rpc.remoting.mxml.RemoteObject;

import org.puremvc.as3.interfaces.INotification;
import org.springextensions.actionscript.puremvc.patterns.command.IocSimpleCommand;

public class GetServiceConfigCommand extends IocSimpleCommand implements IResponder
{
    public static const SUCCESS:String = "GetServiceConfigCommand.SUCCESS";
    public static const FAILURE:String = "GetServiceConfigCommand.FAILURE";

    private var _registry:ServiceRegistry;
    private var _configProxy:ServiceConfigProxy;

    public function GetServiceConfigCommand() {
    }

    public function get registry():ServiceRegistry {
        return _registry;
    }

    public function set registry(value:ServiceRegistry):void {
        _registry = value;
    }

    override public function execute(notification:INotification):void {
        var serviceType:ServiceType = notification.getBody() as ServiceType;
        var service:RemoteObject = registry.getRemoteObjectService(ApplicationFacade.SERVICE_CONFIGURATION_MANAGEMENT_SERVICE);
        var call:Object = service.lookupConfiguration(serviceType);
        call.addResponder(this);
    }

    public function result(data:Object):void {
        var config:ServiceConfiguration = data.result as ServiceConfiguration;
        var serviceType:ServiceType = null;
        if (config != null) {
            if (config is HttpServiceConfiguration) {
                serviceType = ServiceType.HTTP;
                _configProxy.httpService = config as HttpServiceConfiguration;
            } else if (config is SshServiceConfiguration) {
                serviceType = ServiceType.SSH;
                _configProxy.sshService = config as SshServiceConfiguration;
            } else if (config is PersistenceServiceConfiguration) {
                serviceType = ServiceType.PERSISTENCE;
                _configProxy.persistenceService = config as PersistenceServiceConfiguration;
            } else if (config is ManagementServiceConfiguration) {
                serviceType = ServiceType.MANAGEMENT;
                _configProxy.managementService = config as ManagementServiceConfiguration;
            } else if (config is ArtifactQueueManagerConfiguration) {
                serviceType = ServiceType.AQM;
                _configProxy.artifactQueueManagerService = config as ArtifactQueueManagerConfiguration;
            } else if (config is LogServiceConfiguration) {
                serviceType = ServiceType.LOG;
                _configProxy.logService = config as LogServiceConfiguration;
            }
        }

        if (serviceType != null) {
            sendNotification(SUCCESS, serviceType);
        } else {
            sendNotification(FAILURE, serviceType);
        }
    }

    public function fault(info:Object):void {
        trace((info as FaultEvent).fault.message);
        var errorMessage:ErrorMessage = (info as FaultEvent).message as ErrorMessage;
        sendNotification(FAILURE, errorMessage.rootCause.serviceType);
    }

    public function set configProxy(value:ServiceConfigProxy):void {
        _configProxy = value;
    }
}
}