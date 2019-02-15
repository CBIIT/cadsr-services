import { Injectable } from '@angular/core';
import { BehaviorSubject } from '../../../node_modules/rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private reportData = new BehaviorSubject<Object>({'cccForms':[]});
  private loadedFromButton:Boolean;
  private reportLocation:String;

  constructor() { }

  // gets report data as observable //
  getReportData = () => {
    return this.reportData.asObservable();
  };

  // gets report location from session //
  getReportLocation = () => {
    if (!this.loadedFromButton) {
      if (sessionStorage.getItem('reportLocation')) {
        this.reportLocation = this.getSessionDataItem('reportLocation');
      };
    };
    return sessionStorage.getItem('reportLocation')
  }

  // gets session data for report //
  getSessionDataItem = item => {
    let data = sessionStorage.getItem(item);
    try {
      return JSON.parse(sessionStorage.getItem(item));      
    }
    catch {
      return sessionStorage.getItem(item);
    };
  }
  
  // sets session data for report //
  setSessionDataItem = (item,data):void => {
    if (typeof(data)=='string') {
      sessionStorage.setItem(item,data);
    }
    else {
      sessionStorage.setItem(item,JSON.stringify(data));
    };
  };

  // sets report data //
  setReportData = (data):void => {
    this.loadedFromButton = true;
    this.reportData.next(data);
    // this.setSessionDataItem('reportData',data);
  };

  // sets report location in session //
  setReportLocation = (data):void => { 
    this.loadedFromButton = true;
    this.reportLocation = data;
    sessionStorage.setItem('reportLocation',data)  // not json so you dont have to stringify //
  };
  
}

