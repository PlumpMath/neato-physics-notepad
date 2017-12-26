import React from 'react';

const DisplayMath = (props) => {
  const {stuff, ...other} = props;

  return <span>{stuff.map((x, idx) => <DisplayMathElement {...other} el={x} key={idx} />)}</span>;
}

const DisplayMathElement = (props) => {
  const {el, ...other} = props;
  const name = el.name;

  if (name === "span") {
    // TODO: Does it work for me to just set onClick to be `{props.onSpanClick}`
    // rather than wrapping it?
    return <span onMouseDown={(e) => props.onSpanMouseDown(e)}>{el.str}</span>;
  } else if (name === "variableSpan") {
    return <span
      className="equation-var"
      onMouseDown={(e) => props.onVarMouseDown(e, el.varId) }>
      {el.jsEls.map((x, idx) =>
      <DisplayMathElement {...other} key={idx} el={x} onSpanMouseDown={() => {}} />)}
    </span>;
  } else if (name === "sup") {
    return <sup>{el.jsInner.map((x, idx) => <DisplayMathElement {...other} key={idx} el={x} />)}</sup>;
  } else if (name === "sub") {
    return <sub>{el.jsInner.map((x, idx) => <DisplayMathElement {...other} key={idx} el={x} />)}</sub>;
  } else {
    debugger;
  }
}

export default DisplayMath;
