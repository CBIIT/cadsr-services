import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FormListService {
  private formListData = new BehaviorSubject<Object>({"formsList":[],"checkUom":null,"checkStdCrfCde":null,"mustDisplayException":null});
  private checkedItems = new BehaviorSubject<String[]>([]);
  private validItemsLength = new BehaviorSubject<Number>(0);
  private loadedFromFile:Boolean;
  constructor() { 
  }

  // gets form list data as observable. If session has data get that
  getFormListData = () => {
    if (!this.loadedFromFile) {
      if (sessionStorage.getItem('formListData')) {
        this.formListData.next(this.getSessionDataItem('formListData'));     
      };
    };
    return this.formListData.asObservable();
  };

  // gets validation status //
  getValidationStatus = () => {
    const isValidating = this.getSessionDataItem('isValidating');
    return isValidating;
  };

  // gets checked items as observable //
  getCheckedItems = () => {
    if (!this.loadedFromFile) {
      if (sessionStorage.getItem('checkedItems')) {
        this.checkedItems.next(this.getSessionDataItem('checkedItems'));
      };
    };
    return this.checkedItems.asObservable();
  };

  // gets checked status of record //
  getCheckedStatus = record => this.checkedItems.value.indexOf(record.formName) > -1;
  
  // gets valid items length as observable //
  getValidItemsLength = () => this.validItemsLength.asObservable();

  // parse session data //
  getSessionDataItem = item => JSON.parse(sessionStorage.getItem(item));

    // set session data //
  setSessionDataItem = (item,value):void => sessionStorage.setItem(item,JSON.stringify(value));

  // set checkbox array as observable and session value //
  setCheckedItem = (record):void => {
    const checkedItems = this.checkedItems.getValue();
    const itemPosition = checkedItems.indexOf(record.formName);
    itemPosition>-1 ? checkedItems.splice(itemPosition,1) : checkedItems.push(record.formName);
    this.checkedItems.next(checkedItems);
    this.setSessionDataItem('checkedItems',checkedItems);
  };

  // set checkall status. update checkedItems array in session //
  setCheckAllStatus(status):void {
    if (status) {
      const ci = Object.assign([],this.formListData.getValue()['formsList'].filter((r) => r.isValid ).map((e) => e.formName));
      this.checkedItems.next(ci);
      this.setSessionDataItem('checkedItems',ci);
    }
    else {
      this.checkedItems.next([]);
      this.setSessionDataItem('checkedItems',[]);
    };
  };
  
  // sets expand value for record. Used to show hide error for invalid records //
  setExpandCollapse = (record):void => {
    const fld = this.formListData.getValue();
    const matchingIndex = fld['formsList'].findIndex(x => x.formName==record.formName);
    fld['formsList'][matchingIndex]['expand']=!record['expand'];
    this.formListData.next(fld);
  }

  // sets form list data. called from upload form //
  setFormListData = (data):void => { 
    const ci = [];
    this.loadedFromFile = true;
    this.formListData.next(data);
    this.checkedItems.next(ci);
    this.setSessionDataItem('formListData',data) // set formListData in session //
    this.setSessionDataItem('checkedItems',ci); // set checkedItems in session //
  };

  // sets checkUom, checkStdCrfCde and mustDisplayException checkboxes at bottom of page //
  setFormListOptionCheckbox(event):void {
    const fld = this.formListData.getValue();
    const checked = event.target.checked;
    const checkbox = event.target.name;
    fld[checkbox] = checked
    this.formListData.next(fld);
    this.setSessionDataItem('formListData',fld);
  };

  // sets validation status //
  setValidationStatus = (status) => {
    this.setSessionDataItem('isValidating',status);
  };

}