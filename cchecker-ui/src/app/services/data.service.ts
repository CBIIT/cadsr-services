import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  formList:Object;

  constructor() { 
    this.formList = {'formsList':[]};
  }

  // store session storage copy of formList //
  setFormListData = (data) => {
    sessionStorage.setItem('formList',JSON.stringify(data)); // convert to string //
  }

  // get formList data //
  getFormListData = () => sessionStorage.getItem('formList') ? JSON.parse(sessionStorage.getItem('formList')): this.formList;  
}
