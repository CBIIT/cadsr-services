import { Component, OnInit } from '@angular/core';
import { validateConfig } from '@angular/router/src/config';
import { RestService } from '../services/rest.service';
import { HttpClient } from '@angular/common/http'
@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css']
})
export class AlsFormListComponent implements OnInit {
  checkedItems:String[];  // list of checked names //
  formListData:Object;
  validItemsLength:Number;
  selectAllCheckbox:boolean;

  constructor(private restService:RestService, private http:HttpClient) {
    this.selectAllCheckbox = true; // select all forms checkbox. default to all //
    this.checkedItems = []; // array of form names that are selected //
    this.formListData = {'formsList':[]} // data for form list table //
    this.getFormListData(); // get form list data from restService //
   }

  ngOnInit() {
    this.checkedItems = this.setCheckedItemsArray();
  }

  // gets form list data and sets checkedItems array //
  getFormListData() {
    this.formListData = this.restService.formList; 
    this.checkedItems = this.setCheckedItemsArray()
  };

  getCheckedStatus = record => this.checkedItems.indexOf(record.formName) > -1;
  // get usable status message to display in table status column //
  getReadableErrorStatus = e => e ? 'Success':'Fail';

  // sorts data alphabetically by form name //
  getSortedData = () => this.formListData['formsList'].sort((a, b) => a.formName.toUpperCase()<b.formName.toUpperCase() ? -1 : 1);

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
  setCheckedItemsArray = () => Object.assign([],this.formListData['formsList'].filter((r) => r.isValid ).map((e) => e.formName)); 

  // shows or hides error row //
  setExpandCollapse = (e:boolean) => { e['expand']=!e['expand']; return false };
};
