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
    this.data = {"formsList":[{"isValid":false,"errors":[{"errorDesc":"CDE public id and version should be numeric. PID2453601jlkhjh_V1_0_3 { Excel Coordinates | Sheet: Fields | Row: 8 | Cell: 4}.","sheetName":"Fields","rowNumber":8,"colNumber":4,"formOid":"ENROLLMENT","fieldOid":"PT_RACE_CD_3","dataDictionaryName":"CDUS_RACE_C_PID2453600_V6_0_3","unitDictionaryName":null,"errorSeverity":"ERROR","cellValue":"PID2453601jlkhjh_V1_0_3"},{"errorDesc":"CDE public id and version should be numeric. PID2453601sadasdasdsa_V1_0_5 { Excel Coordinates | Sheet: Fields | Row: 10 | Cell: 4}.","sheetName":"Fields","rowNumber":10,"colNumber":4,"formOid":"ENROLLMENT","fieldOid":"PT_RACE_CD_5","dataDictionaryName":"CDUS_RACE_C_PID2453600_V6_0_5","unitDictionaryName":null,"errorSeverity":"ERROR","cellValue":"PID2453601sadasdasdsa_V1_0_5"}],"formName":"Enrollment","questionsCount":31},{"isValid":true,"errors":[],"formName":"Histology and Disease","questionsCount":10},{"isValid":true,"errors":[],"formName":"Administrative Enrollment","questionsCount":10},{"isValid":true,"errors":[],"formName":"Eligibility Checklist","questionsCount":128},{"isValid":true,"errors":[],"formName":"Patient Eligibility","questionsCount":5},{"isValid":true,"errors":[],"formName":"Molecular Marker","questionsCount":4},{"isValid":true,"errors":[],"formName":"Baseline Medical History","questionsCount":6},{"isValid":true,"errors":[],"formName":"Prior Treatment Summary","questionsCount":6},{"isValid":true,"errors":[],"formName":"Prior Therapy Supplement","questionsCount":10},{"isValid":true,"errors":[],"formName":"Prior Surgery Supplement","questionsCount":8},{"isValid":true,"errors":[],"formName":"Prior Radiation Supplement","questionsCount":11},{"isValid":true,"errors":[],"formName":"Baseline Symptoms Presence","questionsCount":3},{"isValid":true,"errors":[],"formName":"Adverse Baseline Symptoms","questionsCount":8},{"isValid":true,"errors":[],"formName":"Course Initiation","questionsCount":14},{"isValid":true,"errors":[],"formName":"Drug Administration","questionsCount":14},{"isValid":true,"errors":[],"formName":"FollowUp","questionsCount":4},{"isValid":true,"errors":[],"formName":"PE","questionsCount":8},{"isValid":true,"errors":[],"formName":"Adverse Event Presence","questionsCount":4},{"isValid":true,"errors":[],"formName":"Adverse Events","questionsCount":20},{"isValid":true,"errors":[],"formName":"Pharmacokinetics Samples","questionsCount":10},{"isValid":true,"errors":[],"formName":"Pharmacokinetics Results","questionsCount":9},{"isValid":true,"errors":[],"formName":"PharmacoDynamics Samples","questionsCount":9},{"isValid":true,"errors":[],"formName":"Pharmacogenetic Samples","questionsCount":8},{"isValid":true,"errors":[],"formName":"Urinary Excretions","questionsCount":26},{"isValid":true,"errors":[],"formName":"Course Assessment","questionsCount":9},{"isValid":true,"errors":[],"formName":"Vital Signs","questionsCount":16},{"isValid":true,"errors":[],"formName":"Serology - Pregnancy - Skin and Stool Tests","questionsCount":19},{"isValid":true,"errors":[],"formName":"Concomitant Measures/Medications","questionsCount":11},{"isValid":true,"errors":[],"formName":"Transfusion","questionsCount":6},{"isValid":true,"errors":[],"formName":"HM","questionsCount":27},{"isValid":true,"errors":[],"formName":"PTC","questionsCount":19},{"isValid":true,"errors":[],"formName":"BCH","questionsCount":22},{"isValid":true,"errors":[],"formName":"BCR","questionsCount":18},{"isValid":true,"errors":[],"formName":"BM","questionsCount":23},{"isValid":true,"errors":[],"formName":"RFB","questionsCount":12},{"isValid":true,"errors":[],"formName":"Literal Laboratory","questionsCount":8},{"isValid":true,"errors":[],"formName":"Unanticipated Lab","questionsCount":12},{"isValid":true,"errors":[],"formName":"IP","questionsCount":16},{"isValid":true,"errors":[],"formName":"OU","questionsCount":19},{"isValid":true,"errors":[],"formName":"RC","questionsCount":25},{"isValid":true,"errors":[],"formName":"RF","questionsCount":16},{"isValid":true,"errors":[],"formName":"SC","questionsCount":16},{"isValid":true,"errors":[],"formName":"SE","questionsCount":16},{"isValid":true,"errors":[],"formName":"US","questionsCount":19},{"isValid":true,"errors":[],"formName":"UE","questionsCount":16},{"isValid":true,"errors":[],"formName":"LS","questionsCount":20},{"isValid":true,"errors":[],"formName":"New Lesion Presence","questionsCount":3},{"isValid":true,"errors":[],"formName":"NLS","questionsCount":20},{"isValid":true,"errors":[],"formName":"Off Treatment / Off Study","questionsCount":9},{"isValid":true,"errors":[],"formName":"Follow Up","questionsCount":4},{"isValid":true,"errors":[],"formName":"Death Summary","questionsCount":17},{"isValid":true,"errors":[],"formName":"Study Conclusion","questionsCount":6},{"isValid":true,"errors":[],"formName":"CS","questionsCount":10},{"isValid":true,"errors":[],"formName":"EX","questionsCount":32},{"isValid":true,"errors":[],"formName":"Subject Enrollment","questionsCount":10},{"isValid":true,"errors":[],"formName":"Demography","questionsCount":10},{"isValid":true,"errors":[],"formName":"Step Information","questionsCount":14},{"isValid":true,"errors":[],"formName":"Treatment Assignment","questionsCount":9},{"isValid":true,"errors":[],"formName":"Literal Extra","questionsCount":15},{"isValid":true,"errors":[],"formName":"Genetic Markers","questionsCount":9},{"isValid":true,"errors":[],"formName":"CLARKP_cdeCart_1","questionsCount":1},{"isValid":true,"errors":[],"formName":"Biomarkers","questionsCount":9},{"isValid":true,"errors":[],"formName":"Age List","questionsCount":3},{"isValid":true,"errors":[],"formName":"CLARKP_cdeCart","questionsCount":3},{"isValid":true,"errors":[],"formName":"Comments","questionsCount":5},{"isValid":true,"errors":[],"formName":"Patient Information for NCI Reporting","questionsCount":13},{"isValid":true,"errors":[],"formName":"Late Adverse Event Presence","questionsCount":2},{"isValid":true,"errors":[],"formName":"Late Adverse Events","questionsCount":17},{"isValid":true,"errors":[],"formName":"Creatinine Clearance","questionsCount":9},{"isValid":true,"errors":[],"formName":"sarakhan_cdeCart","questionsCount":1},{"isValid":true,"errors":[],"formName":"Hidden Labels","questionsCount":10},{"isValid":true,"errors":[],"formName":"Specimen Tracking Enrollment","questionsCount":8},{"isValid":true,"errors":[],"formName":"Tracking Contacts","questionsCount":3},{"isValid":true,"errors":[],"formName":"Specimen Transmittal","questionsCount":22},{"isValid":true,"errors":[],"formName":"Copy Shipping","questionsCount":5},{"isValid":true,"errors":[],"formName":"Shipping Status","questionsCount":18},{"isValid":true,"errors":[],"formName":"Modified Severity Weighted Assessment (mSWAT)","questionsCount":10}],"checkUom":null,"checkStdCrfCde":null,"mustDisplayException":null};
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
  setExpandCollapse = (e:boolean) => { e['expand']=!e['expand']; return false };
};
