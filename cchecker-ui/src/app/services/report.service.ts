import { Injectable } from '@angular/core';
import { BehaviorSubject } from '../../../node_modules/rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private reportData = new BehaviorSubject<Object>({'cccForms':[]});
  private loadedFromButton:Boolean;

  constructor() { }

  // gets report data as observable //
  getReportData = () => {
    if (!this.loadedFromButton) {
      if (sessionStorage.getItem('reportData')) {
        this.reportData.next(this.getSessionDataItem('reportData'));
      };
    };
    return this.reportData.asObservable();
  };

  // gets status of excel generating //
  getExcelGenStatus = () => {
    const isGenerating = this.getSessionDataItem('isGenerating');
    return isGenerating;
  };

  // gets session data for report //
  getSessionDataItem = item => JSON.parse(sessionStorage.getItem(item));
  
  // sets session data for report //
  setSessionDataItem = (item,data):void => sessionStorage.setItem(item,JSON.stringify(data));

  // sets report data //
  setReportData = (data):void => {
    this.loadedFromButton = true;
    this.reportData.next(data);
    this.setSessionDataItem('reportData',data);
  };

  // sets excel generation status //
  setExcelGenStatus = (isGenerating) => {
    this.setSessionDataItem('isGenerating',isGenerating);
  };
}

