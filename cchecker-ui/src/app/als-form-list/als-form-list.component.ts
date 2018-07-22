import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css']
})
export class AlsFormListComponent implements OnInit {
  data:Object;
  checkedItems:string[];  // list of checked names //

  constructor() {
    // test data //
    this.data = {"formsList":[{"isValid":false,"errors":[],"formName":"Enrollment","questionsCount":31},{"isValid":true,"errors":[],"formName":"Histology and Disease","questionsCount":10},{"isValid":false,"errors":[],"formName":"Administrative Enrollment","questionsCount":10},{"isValid":false,"errors":[],"formName":"Eligibility Checklist","questionsCount":128},{"isValid":true,"errors":[],"formName":"Patient Eligibility","questionsCount":5},{"isValid":true,"errors":[],"formName":"Molecular Marker","questionsCount":4}],"checkUom":null,"checkStdCrfCde":null,"mustDisplayException":null};
   }
   
  ngOnInit() {
  }

  // get usable status message to display in table status column //
  getStatus = (isValid:boolean) => isValid ? 'Success':'Fail';

  // shows or hides error row //
  showHideError = (i:boolean) => { i['expand']=!i['expand']; return false }

  // sorts data alphabetically by form name //
  sortedData = () => this.data['formsList']
      .sort((a, b) => a.formName.toUpperCase()<b.formName.toUpperCase() ? -1 : 1);
};
