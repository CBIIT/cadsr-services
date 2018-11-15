import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';


@Injectable({
  providedIn: 'root'
})

export class RestService {
  path:string;

  constructor(private http:HttpClient, private router:Router) {
    this.path = `${window.location.protocol}//${window.location.hostname}:8080`;
  }

  // generate excel report //
  generateExcel = () => this.http.get(`${this.path}/gateway/genexcelreporterror`,
  {
    observe:'response',
    responseType: "blob",
    withCredentials:true
  }
  );

  // validation service //
  checkForms(checkedItems,formListData){
    const checkUom = formListData['checkUom'] ? 'true':'false';
    const checkCRF = formListData['checkStdCrfCde'] ? 'true':'false';
    const displayExceptions = formListData['mustDisplayException'] ? 'true':'false';
    return this.http.post(`${this.path}/gateway/checkservice`,checkedItems,
    {
      withCredentials:true
    })
  } ; 

  // upload file service //
  uploadAlsFile(file, name){
    console.log(this.path)
    return this.http.post(`${this.path}/gateway/parseservice?owner=${name}`,file,
    {
      observe:"events",
      reportProgress:true,
      withCredentials:true,
    })} ;

  // gets validation status //
  validateFeedStatus() {
    return this.http.get(`${this.path}/gateway/feedvalidatestatus`,
    {
      observe:'events',
      reportProgress:true,
      responseType: "text",
      withCredentials:true
    });
  }




}
