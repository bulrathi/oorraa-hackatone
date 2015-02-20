var converter = new Showdown.converter();

var Chat = React.createClass({
  render: function() {
    var rawMarkup = converter.makeHtml(this.props.children.toString());
    return (
      <div className="chat">
        <h3 className="chatAuthor">
          {this.props.author}
        </h3>
        <span dangerouslySetInnerHTML={{__html: rawMarkup}} />
      </div>
    );
  }
});

var ChatBox = React.createClass({
  loadChatsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleChatSubmit: function(chat) {
    var chats = this.state.data;
    chats.push(chat);
    this.setState({data: chats}, function() {
      $.ajax({
        url: this.props.url,
        dataType: 'json',
        type: 'POST',
        data: chat,
        success: function(data) {
          this.setState({data: data});
        }.bind(this),
        error: function(xhr, status, err) {
          console.error(this.props.url, status, err.toString());
        }.bind(this)
      });
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadChatsFromServer();
    setInterval(this.loadChatsFromServer, this.props.pollInterval);
  },
  render: function() {
    return (
      <div className="chatBox">
        <h1>Chat</h1>
        <ChatForm onChatSubmit={this.handleChatSubmit} />
	<ChatList data={this.state.data} />
      </div>
    );
  }
});

var ChatList = React.createClass({
  render: function() {
    var chatNodes = this.props.data.map(function(chat, index) {
      return (
        <Chat author={chat.author} key={index}>
          {chat.text}
        </Chat>
      );
    });
    return (
      <div className="chatList">
        {chatNodes}
      </div>
    );
  }
});

var ChatForm = React.createClass({
  handleSubmit: function(e) {
    e.preventDefault();
    var author = "browser"//this.refs.author.getDOMNode().value.trim();
    var text = this.refs.text.getDOMNode().value.trim();
    if (!text) {
      return;
    }
    this.props.onChatSubmit({author: author, text: text});
    this.refs.author.getDOMNode().value = '';
    this.refs.text.getDOMNode().value = '';
  },

  render: function() {
    return (
      <form className="chatForm" onSubmit={this.handleSubmit} class="tweet">
        <input type="text" placeholder="Message..." ref="text" />&nbsp;&nbsp;
        <input type="submit" value="Send" />
      </form>
    );
  }
});

React.render(
  <ChatBox url="chat.json" pollInterval={2000} />,
  document.getElementById('content')
);
