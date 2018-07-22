import { Component, OnInit } from '@angular/core';
import { validateConfig } from '../../../node_modules/@angular/router/src/config';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css']
})
export class AlsFormListComponent implements OnInit {
  checkedItems:String[];  // list of checked names //
  data:Object;
  validItemsLength:Number;
  selectAllCheckbox:boolean;

  constructor() {
    // test data //
    this.data = {"formsList":[{"isValid":false,"errors":[],"formName":"Enrollment","questionsCount":31},{"isValid":true,"errors":[],"formName":"Histology and Disease","questionsCount":10},{"isValid":false,"errors":[],"formName":"Administrative Enrollment","questionsCount":10},{"isValid":false,"errors":[],"formName":"Eligibility Checklist","questionsCount":128},{"isValid":true,"errors":[],"formName":"Patient Eligibility","questionsCount":5},{"isValid":true,"errors":[],"formName":"Molecular Marker","questionsCount":4}],"checkUom":null,"checkStdCrfCde":null,"mustDisplayException":null};
    // end test data //

    this.checkedItems = this.setCheckedItemsArray();
    this.selectAllCheckbox = true; // select all forms checkbox. default to all //
    this.validItemsLength = this.checkedItems.length;
   }
  ngOnInit() {
  }

  // gets if record should be checked or not //
  getCheckedStatus = record => this.checkedItems.indexOf(record.formName) > -1;
  
  // get usable status message to display in table status column //
  getReadableErrorStatus = e => e ? 'Success':'Fail';

  // sorts data alphabetically by form name //
  getSortedData = () => this.data['formsList'].sort((a, b) => a.formName.toUpperCase()<b.formName.toUpperCase() ? -1 : 1);

  // add or remove record from checkedItems array //
  setCheckedItem = e => { 
    this.checkedItems.indexOf(e['formName'])==-1 ? 
    this.checkedItems.push(e['formName']):this.checkedItems.splice(this.checkedItems.indexOf(e['formName']),1);
    this.validItemsLength==this.checkedItems.length ? this.selectAllCheckbox = true : this.selectAllCheckbox = false; 
    return false;
  };

  // sets checked all items to all or none //
  setCheckAllStatus = e => e.target.checked ? this.checkedItems = this.setCheckedItemsArray() : this.checkedItems = [];

  // filter function to assign ALL valid records to checkedItems array //
  setCheckedItemsArray = () => Object.assign([],this.data['formsList'].filter((r) => r.isValid ).map((e) => e.formName)); 

  // shows or hides error row //
  setExpandCollapse = (e:boolean) => { e['expand']=!e['expand']; console.log(e['expand']; return false };
};
