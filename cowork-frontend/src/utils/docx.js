import mammoth from 'mammoth'
import axios from '../api'
import store from '@/store'
import Quill from "quill"
import Sequence from "./sequence"

function readFileInputEventAsArrayBuffer(callback, onSelect, selectError) {
  let fileInput = document.createElement('input')
  fileInput.setAttribute('type', 'file')
  fileInput.setAttribute(
    'accept',
    '.doc,.docx,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  )
  fileInput.addEventListener('change', () => {
    const files = fileInput.files

    if (files == null && files[0] == null) {
      return
    }

    onSelect()

    let fileName = files[0].name.replace(/\.[^/.]+$/, "")

    let reader = new FileReader();
    reader.onload = loadEvent => {
      let arrayBuffer = loadEvent.target.result;
      callback(fileName, arrayBuffer);
    };
    reader.onerror = () => { selectError() }

    reader.readAsArrayBuffer(files[0]);
  })
  fileInput.click()
}

function uploadDocNodes(uploadPath, name, crdts, uploadSuccess, uploadError) {
  axios
    .post(uploadPath, {
      docName: name,
      crdts: crdts['crdts'],
    })
    .then(res => {
      if (res.data.code === 200) {
        uploadSuccess(res.data.msg)
      } else uploadError(res.data.msg)
    })
    .catch(err => uploadError(err))
}

function parseJwt(token) {
  let base64Url = token.split('.')[1]
  let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  let jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      })
      .join('')
  )
  return JSON.parse(jsonPayload)
}

function readDocFile(
  uploadPath = '/doc/uploadDoc',
  onSelect = () => { },
  uploadSuccess = () => { },
  uploadError = () => { },
  selectError = () => { }) {

  const sequence = new Sequence({
    version: 0,
    uid: parseJwt(store.state.accessToken)['id']
  })

  readFileInputEventAsArrayBuffer((name, arrayBuffer) => {

    mammoth.convertToHtml({ arrayBuffer: arrayBuffer })
      .then((result) => {
        console.log(result.value);
        let node = document.createElement('div')

        let quill = new Quill(node, {})
        quill.root.innerHTML = result.value
        quill.update()

        let crdts = sequence.applyDelta(quill.getContents())
        uploadDocNodes(uploadPath, name, crdts, uploadSuccess, uploadError)
      }).catch(() => selectError()).done();
  }, onSelect, selectError);
}

export default readDocFile