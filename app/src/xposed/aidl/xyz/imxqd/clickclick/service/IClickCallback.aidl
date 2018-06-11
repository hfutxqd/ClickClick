// IClickCallback.aidl
package xyz.imxqd.clickclick.service;

import xyz.imxqd.clickclick.service.Command;
import xyz.imxqd.clickclick.service.Result;

interface IClickCallback {
    Result send(in Command cmd);
}
