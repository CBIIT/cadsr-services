import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css']
})
export class AlsFormListComponent implements OnInit {
  data:Object;
  constructor() {
    this.data = {"formsList":[{"isValid":false,"errors":[],"formName":"Enrollment","questionsCount":31},{"isValid":true,"errors":[],"formName":"Histology and Disease","questionsCount":10},{"isValid":false,"errors":[],"formName":"Administrative Enrollment","questionsCount":10},{"isValid":false,"errors":[],"formName":"Eligibility Checklist","questionsCount":128},{"isValid":true,"errors":[],"formName":"Patient Eligibility","questionsCount":5},{"isValid":true,"errors":[],"formName":"Molecular Marker","questionsCount":4}],"checkUom":null,"checkStdCrfCde":null,"mustDisplayException":null};
   }
  ngOnInit() {
  }

  getStatus(isValid:boolean) {
    if (isValid) {
      return 'Success';
    }
    else {
      return 'Fail';
    };
  };

  sortedData() {
    this.data['formsList'].sort(function (a, b) {
      if (a.formName.toUpperCase()<b.formName.toUpperCase()) {
        return -1
      }
      else {
        return 1
      }
    });
    return this.data;
  }

  showDetails(i) {
    if (!i['expand']) {
      i['expand']=false;
    }
    i['expand']=!i['expand'];
    return false;
  };

}
