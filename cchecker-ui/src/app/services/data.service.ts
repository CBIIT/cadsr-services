import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  constructor() { 
  }

  // get formList data //
  getFormListData = () => {
    const formList = {'formsList':[],'checkUom':null,'checkStdCrfCde':null,'mustDisplayException':null}; // empty formlist //
    return sessionStorage.getItem('formList') ? JSON.parse(sessionStorage.getItem('formList')): formList; 
  }

  // parse string JSON data from session. get value of checkboxes.  //
  getFormListOptionCheckbox = checkboxes => {  
    const formListData = JSON.parse(sessionStorage.getItem('formList'))
    const checkboxObject = {};
    for (let c=0; c<checkboxes.length;c++) {
      formListData ? checkboxObject[checkboxes[c]]=formListData[checkboxes[c]] : checkboxObject[checkboxes[c]]=false;
    };
    return checkboxObject;
  }

  // store session storage copy of formList //
  setFormListData = (data) => {
    sessionStorage.setItem('formList',JSON.stringify(data)); // convert to string //
  }

  // parse string JSON data from session. set value of checkbox. re-string JSON data and save to session //
  setFormListOptionCheckbox(name,value) {  
    const formListData = JSON.parse(sessionStorage.getItem('formList'))
    if (formListData) {
      formListData[name]=value; // set checkbox value in sessionStorage data //
      sessionStorage.setItem('formList',JSON.stringify(formListData)); // update formList session variable //
    };
  }

}
