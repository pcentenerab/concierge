import React, { Component } from 'react';
import CustomRequestForm from "./CustomRequestForm"
import { withRouter } from "react-router-dom"

class CustomRequestPage extends Component {
    render() {
        if(! this.props.logged){
            this.props.history.replace("/login")
        }
        return (
            <CustomRequestForm sendForm={this.sendForm.bind(this)} login={this.login.bind(this)} />
        );

    }

    login(json){
        this.props.login(json)
    }

    sendForm(){
        console.log("sendForm")
    }
}

export default withRouter(CustomRequestPage);
