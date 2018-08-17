import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  constructor(private http:HttpClient) {
  }

  // generate excel report //
  generateExcel = () => this.http.get('http://localhost:8080/gateway/genexcelreporterror',
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
    return this.http.post('http://localhost:8080/gateway/checkservice',checkedItems,
    {
      withCredentials:true
    })
  } ; 

  // upload file service //
  uploadAlsFile(file){
    return this.http.post('http://localhost:8080/gateway/parseservice?owner=me2',file,
    {
      observe:"events",
      reportProgress:true,
      withCredentials:true,
    })} ;





}
